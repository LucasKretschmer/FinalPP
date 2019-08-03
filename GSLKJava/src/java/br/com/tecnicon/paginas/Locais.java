package br.com.tecnicon.paginas;

import br.com.tecnicon.componente.CriaComponentesHtml;
import br.com.tecnicon.control.TTranslate;
import br.com.tecnicon.controller.EncerarSistema;
import br.com.tecnicon.police.ListaUsuarios;
import br.com.tecnicon.server.context.TClassLoader;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.dataset.TSQLDataSetEmp;
import br.com.tecnicon.server.dataset.TSQLDataSetTec;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.interfaces.TTela;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.io.Serializable;
import javax.ejb.Stateless;

/**
 *
 * @author jean.siqueira
 */
@Stateless
public class Locais implements TTela, Serializable
{

    @Override
    public String obterTelaHtml(VariavelSessao vs)
    {
        try
        {
            String filial = vs.getParameter("filial");
            ListaUsuarios listaUs = ListaUsuarios.getInstance();

            if (filial != null && !filial.equals(""))
            {
                vs.addParametros("filial", filial);

                if (vs.getParameter("nomeFilial") != null && !vs.getParameter("nomeFilial").trim().equals(""))
                {
                    vs.addParametros("nomeFilial", vs.getParameter("nomeFilial"));
                    vs.addParametros("nomefilial", vs.getParameter("nomeFilial"));
                }
            } else
            {
                filial = vs.getValor("filial");
            }

            if (vs.getParameter("SIGLAFILIAL") == null || vs.getParameter("SIGLAFILIAL").trim().equals(""))
            {
                TSQLDataSetEmp sigla = TSQLDataSetEmp.create(vs);
                sigla.commandText("SELECT FILIAL.SIGLA FROM FILIAL WHERE FILIAL.CFILIAL = " + filial);
                sigla.open();
                if (sigla.recordCount() > 0)
                {
                    vs.addParametros("siglafilial", sigla.fieldByName("SIGLA").asString());
                }
                sigla.close();
            } else
            {
                vs.addParametros("siglafilial", vs.getParameter("SIGLAFILIAL").trim());
            }

            if ("undefined".equals(filial))
            {
                new EncerarSistema().encerraSessao(vs, "Sessão sem filial definida ao definir local");
                return "erro:Filial não selecionada";
            }

            if (vs.getValor("empresa").equals(""))
            {
                new EncerarSistema().encerraSessao(vs, "Sessão sem empresa definida ao definir local");
                return "erro:Ocorreu um erro ao tentar logar. Por favor efetue o login novamente.";
            }

            TClientDataSet cdsUsu = TClientDataSet.create(vs, "USUARIO");
            cdsUsu.createDataSet();
            cdsUsu.condicao(" WHERE USUARIO.CUSUARIO = " + vs.getValor("cusuario"));
            cdsUsu.open();

            if (!cdsUsu.fieldByName("CEMPRESA").asString().equals(vs.getValor("empresa"))
                    || (!cdsUsu.fieldByName("CFILIAL").asString().equals(filial) && filial != null))
            {
                listaUs.setCempresaUsuario(vs, vs.getValor("cusuario"), Integer.parseInt(vs.getValor("empresa")));
                listaUs.setCfilialUsuario(vs, vs.getValor("cusuario"), Integer.parseInt(filial));
                TSQLDataSetTec cdsUsuU = TSQLDataSetTec.create(vs);
                cdsUsuU.commandText("UPDATE USUARIO SET CEMPRESA = " + vs.getValor("empresa") + ", CFILIAL = "
                        + filial + " WHERE USUARIO.CUSUARIO = " + vs.getValor("cusuario"));
                cdsUsuU.execSQL();
            }

            TClientDataSet sqlemp = TClientDataSet.create(vs, "LOCAL");
            sqlemp.createDataSet();
            sqlemp.condicao(" WHERE EXISTS (SELECT GRUSUARIOLOCAL.CLOCAL"
                    + " FROM GRUSUARIOLOCAL"
                    + " WHERE GRUSUARIOLOCAL.CLOCAL = LOCAL.CLOCAL"
                    + " AND GRUSUARIOLOCAL.CGRUSUARIO IN ( " + Grupos.obterGrupos(vs) + ")  )");
            sqlemp.open();

            if (sqlemp.recordCount() <= 0)
            {
                new EncerarSistema().encerraSessao(vs, "Usuário não possuí locais definidos para o grupo de usuário");
                return "erro:" + TTranslate.translate(vs, "Não há locais disponíveis para o seu grupo de usuário");
            }

            TClassLoader.execMethod("TecniconPadroesVS/TecniconPadroesVS", "preenchePadroesVS", vs);

            String sql = "SELECT CAST(RDB$GET_CONTEXT('SYSTEM', 'ENGINE_VERSION') AS VARCHAR(10)) AS VERSAODB,"
                    + " CAST(VERSAOEMP.VERSAO AS VARCHAR(10)) AS VERSAOEMP"
                    + " FROM VERSAOEMP";

            TSQLDataSetEmp sqlEMP = TSQLDataSetEmp.create(vs);
            sqlEMP.commandText(sql);
            sqlEMP.open();

            StringBuilder versao = new StringBuilder(sqlEMP.fieldByName("VERSAOEMP").asString());
            versao.insert(4, ".");
            versao.insert(7, ".");//formatação certa: ####.##.## desde 2004

            String versaoDB = sqlEMP.fieldByName("VERSAODB").asString();

            vs.addParametros("versao", versao.toString());
            vs.addParametros("versaobanco", versaoDB);
            if (sqlemp.recordCount() == 1)
            {

                vs.addParametros("local", sqlemp.fieldByName("CLOCAL").asString());
                vs.addParametros("nomeLocal", sqlemp.fieldByName("NOME").asString());
                vs.addParametros("nomelocal", sqlemp.fieldByName("NOME").asString());

                return (String) TClassLoader.execMethod("TecniconLogin/TelaPropriedade", "selecionarPropriedade", vs);
            }

            try
            {
                return telaSelecionaLocais(vs);
            } catch (Exception ex)
            {
                return "erro:" + ex.getMessage();
            }

        } catch (ExcecaoTecnicon ex)
        {
            //ex.printStackTrace();
            return TTranslate.translate(vs, "Erro ao Buscar Locais. Detalhes:") + ex.getMessage();
        }
    }

    private String telaSelecionaLocais(VariavelSessao vs) throws ExcecaoTecnicon
    {
        final String idDialog = vs.getParameter("iddialog");
        final String grupos = Grupos.obterGrupos(vs);

        StringBuilder sb = new StringBuilder(1024);
        sb.append("<div role=\"dialog\" painel=\"false\" id=\"").append(idDialog).append("\" class=\"div-fundo\" modal=\"false\" ttipo=\"TelaDados\" centralizartela=\"false\"  style=\"top:0px;max-width:800px;left:0px;width:100%;height:325px;\">\n"
                + "    <div class=\"div-header-dialog gradiente-color\">\n"
                + "        <label caption=\"Selecione o Local\" class=\"label-dialog\">Selecione o Local</label></div><div class=\"div-ancora\" id=\"div-ancora\" tid=\"div-ancora\"><div class=\"div-dados-extended\" dadosjs=\"true\" id=\"div0\" tid=\"div0\"><div id=\"tLocais0\" tp=\"pop\" class=\"\" style=\"max-width:800px;width:100%;\" >\n"
                + "    <script>function manFoco(){ quemsou = document.querySelector('#" + idDialog + "'); } quemsou = document.querySelector('#" + idDialog + "'); jQuery(quemsou).find(\"#btnCancelarValida\").focus(); window.fnSelecionarLocal = function(btn) {"
                + "             selecionarLocal2(btn,'").append(vs.getValor("sessao")).append("','").append(vs.getValor("cusuario")).append("','")
                .append(vs.getValor("nome")).append("','").append(vs.getValor("email")).append("'").append(",'").append(vs.getValor("versao"))
                .append("','").append(vs.getValor("versaobanco")).append("','").append(vs.getValor("empresa")).append("','").append(vs.getValor("filial")).append("',")
                .append("     '").append(vs.getValor("nomeempresa")).append("','").append(vs.getValor("nomefilial")).append("',undefined,undefined);")
                .append("   selecionouLocal2(btn);"
                        + "         };</script><div style=\"height: 200px; position: relative; top: 0; width: 100%; -webkit-box-sizing: border-box; -moz-box-sizing: border-box; box-sizing: border-box;\">");

        TSQLDataSetEmp con = TSQLDataSetEmp.create(vs);
        con.close();
        con.commandText(" SELECT PARAMETROEMP.BLOCALINATIVO FROM PARAMETROEMP");
        con.open();
        String localativo = "";
        if ("S".equals(con.fieldByName("BLOCALINATIVO").asString()))
        {
            localativo = " LOCAL.ATIVO='S' AND ";
        }
        TClientDataSet cdsLocais = TClientDataSet.create(vs, "LOCALLOGIN");
        cdsLocais.createDataSet();
        cdsLocais.condicao("WHERE " + localativo + "LOCAL.CLOCAL IN (SELECT GRUSUARIOLOCAL.CLOCAL FROM GRUSUARIOLOCAL WHERE GRUSUARIOLOCAL.CGRUSUARIO IN ( " + grupos + " ))");
        cdsLocais.open();

        CriaComponentesHtml html = new CriaComponentesHtml();
        html.vsInt = vs;

        sb.append(html.criaGrid(cdsLocais, "con_LOCALLOGIN", "camposVisiveis=CLOCAL,NOME;", ""));

        sb.append("</div><input caption=\"Selecionar\" class=\"btn-padrao\" id=\"btnCancelarValida\"  onclick=\"fnSelecionarLocal(this);\"");
        sb.append(" type=\"button\" value=\"Selecionar\"></input>"
                + "<script>var bname=navigator.appName; var bversion=navigator.appVersion; "
                + "     if(bname == \"Microsoft Internet Explorer\" || bversion.indexOf(\"JavaFX\") > 0) { if(bversion[0] == 4 || bversion.indexOf(\"JavaFX\") > 0) { "
                + "         jQuery('.btnSelLocal').get(0).onkeydown = function() { if(event.keyCode != 13) return;");
        sb.append("         selecionarLocal2(this,'").append(vs.getValor("sessao")).append("','").append(vs.getValor("cusuario")).append("','").append(vs.getValor("nome")).append("','").append(vs.getValor("email")).append("'"
                + "         ,'").append(vs.getValor("versao")).append("','").append(vs.getValor("versaobanco")).append("','").append(vs.getValor("empresa")).append("','").append(vs.getValor("filial")).append("',"
                + "         '").append(vs.getValor("nomeempresa")).append("','").append(vs.getValor("nomefilial")).append("',undefined,undefined);"
                + "         selecionouLocal2(this); } } }"
                + "   </script></div></div></div>"
                + " <script>addFuncDialog('").append(idDialog).append("');");
        sb.append(" ultimozindex = document.querySelector('#").append(idDialog).append("'); zindexJanela++; num++;  contador++; tDialog(jQuery('#").append(idDialog).append("').get(0),document.body,{destroy:true, funcfecha:'',modal:'false' }); </script></div>");

        return sb.toString();
    }
}
