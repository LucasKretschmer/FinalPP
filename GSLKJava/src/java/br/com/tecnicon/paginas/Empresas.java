package br.com.tecnicon.paginas;

import br.com.tecnicon.componente.CriaComponentesHtml;
import br.com.tecnicon.control.TTranslate;
import br.com.tecnicon.police.TValidaSessao;
import br.com.tecnicon.server.Propriedades;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import javax.ejb.Stateless;

/**
 *
 * @author jean.siqueira
 */
@Stateless
public class Empresas
{

    public String obterTelaHtml(VariavelSessao vs)
    {
        try
        {
            String eliminarSessao = vs.getParameter("eliminarSessao");

            if (eliminarSessao != null && eliminarSessao.equals("S"))
            {
                vs.addParametros("motivo_encerra", "Sessão encerrada pelo usuário na tela sessões ativas, após o login");
                TValidaSessao.getInstance().excluirSessoesAtivas(vs);
            }

            String ret = ControlLogin.ValidaQtdeSessoes(vs, true, 2);
            if (ret.startsWith("erro:"))
            {
                return ret;
            }

            br.com.tecnicon.server.bd.entity.Sessao sessao = br.com.tecnicon.police.Util.getSessaoEmp(vs, vs.getValor("sessao"));
            
            if (sessao != null)
            {
                sessao.antesLogin = false;
            } else
            {
                return "erro:Falha ao criar sessão";
            }

            if (vs.getValor("cusuario") != null && vs.getValor("cusuario").equals(""))
            {
                return "erro:Ocorreu um erro no seu processo de login. Entre em contato com a tecnicon";
            }

            TClientDataSet cdsEmpresa = TClientDataSet.create(vs, "EMPRESA");
            cdsEmpresa.createDataSet();

            if (Propriedades.isServidorCloud() || Propriedades.isSemBancoTecnicon())
            {
                cdsEmpresa.condicao("WHERE EMPRESA.CEMPRESA IN (" + vs.getValor("empresa") + ")");
                cdsEmpresa.open();

                String empresa = cdsEmpresa.fieldByName("cempresa").asString();

                vs.addParametros("empresa", empresa);
                vs.addParametros("empresaRef", empresa);
                vs.addParametros("nomeEmpresa", cdsEmpresa.fieldByName("RAZAO_SOCIAL").asString());
                vs.addParametros("nomeempresa", cdsEmpresa.fieldByName("RAZAO_SOCIAL").asString());

                Filiais fil = new Filiais();
                return fil.obterTelaHtml(vs);
            }

            TClientDataSet cdsGrupoUsuario = TClientDataSet.create(vs, "USUARIOGRUPO");
            cdsGrupoUsuario.createDataSet();
            cdsGrupoUsuario.condicao(" WHERE USUARIOGRUPO.CUSUARIO = " + vs.getValor("cusuario"));
            cdsGrupoUsuario.open();

            String grupo = cdsGrupoUsuario.fieldByName("CGRUSUARIO").asString();
            vs.addParametros("cgrupo", grupo);

            String condicao2 = " WHERE EMPRESA.ATIVO='S' AND EMPRESA.CEMPRESA IN(SELECT GRUSUARIOEMP.CEMPRESA "
                    + " FROM GRUSUARIOEMP "
                    + " WHERE GRUSUARIOEMP.CGRUSUARIO IN ( SELECT CGRUSUARIO FROM USUARIOGRUPO WHERE USUARIOGRUPO.CUSUARIO =  " + vs.getValor("cusuario") + "))";

            if (!vs.getValor("emaillogin").equals("") || (vs.getValor("tipologin") != null && vs.getValor("tipologin").equals("3")))
            {
                condicao2 += " AND EMPRESA.CEMPRESA IN (" + vs.getValor("cempresacliente") + ")";
            }

            if (vs.getValor("tipologin") != null && vs.getValor("tipologin").equals("C"))
            {
                condicao2 += " AND EMPRESA.CEMPRESA IN (" + vs.getValor("empresa") + ")";
            }

            cdsEmpresa.condicao(condicao2);
            cdsEmpresa.open();

            if (cdsEmpresa.recordCount() <= 0)
            {
                return "erro:" + TTranslate.translate(vs, "Nenhuma empresa liberada para o usuário");
            } else if (cdsEmpresa.recordCount() == 1)
            {
                String empresa = cdsEmpresa.fieldByName("cempresa").asString();
                vs.addParametros("empresa", empresa);
                vs.addParametros("empresaRef", empresa);
                vs.addParametros("nomeEmpresa", cdsEmpresa.fieldByName("RAZAO_SOCIAL").asString());
                vs.addParametros("nomeempresa", cdsEmpresa.fieldByName("RAZAO_SOCIAL").asString());

                Filiais fil = new Filiais();
                return fil.obterTelaHtml(vs);
            } else
            {

                try
                {
                    return telaSelecionaEmpresa(vs, condicao2);
                } catch (Exception ex)
                {
                    return "erro:" + ex.getMessage();
                }
            }
        } catch (ExcecaoTecnicon ex)
        {
            //ex.printStackTrace();
            
            return TTranslate.translate(vs, "Erro ao Buscar empresas");
        }
    }

    public String ultimaLogada(VariavelSessao vs)
    {
        try
        {
            TClientDataSet cds = TClientDataSet.create(vs, "USUARIO");
            cds.createDataSet();
            cds.condicao(" WHERE USUARIO.CUSUARIO = " + vs.getValor("cusuario"));
            cds.open();

            if (!cds.fieldByName("CEMPRESA").asString().equals(""))
            {
                return cds.fieldByName("CEMPRESA").asString();
            }

            return "";
        } catch (ExcecaoTecnicon ex)
        {
            return "";
        }
    }

    private String telaSelecionaEmpresa(VariavelSessao vs, String condicao) throws ExcecaoTecnicon
    {
        final String idDialog = vs.getParameter("iddialog");

        StringBuilder sb = new StringBuilder(1024);
        sb.append("<div role=\"dialog\" painel=\"false\" id=\"" + idDialog + "\" class=\"div-fundo\" modal=\"false\" style=\"top:0px;max-width:800px;left:0px;width:100%;height:325px;\">\n"
                + "    <div class=\"div-header-dialog gradiente-color\">\n"
                + "        <label caption=\"Selecione a Empresa\" class=\"label-dialog\">Selecione a Empresa</label></div><div class=\"div-ancora\" id=\"div-ancora\"  tid=\"div-ancora\"><div class=\"div-dados-extended\" dadosjs=\"true\" id=\"" + idDialog + "\"  tid=\"" + idDialog + "\"><div ttipo=\"tEmpresas\" tipo=\"H\" onmouseup=\"\" onclick=\"\" onmousedown=\"\" id=\"tEmpresas0\" class=\"\" style=\"max_width:800px;max-width:800px;width:100%;position:initial;\" >\n"
                + "    <script>function manFoco(){ quemsou = document.querySelector('#" + idDialog + "');  } var quemsou =  document.querySelector('#" + idDialog + "');</script>"
                + " <div style=\"height: 200px; position: relative; top: 0; width: 100%; -webkit-box-sizing: border-box; -moz-box-sizing: border-box; box-sizing: border-box;\">");

        TClientDataSet cdsEmpresaLogin = TClientDataSet.create(vs, "EMPRESALOGIN");
        cdsEmpresaLogin.createDataSet();
        cdsEmpresaLogin.condicao(condicao);
        cdsEmpresaLogin.ordenar("ORDER BY EMPRESA.RAZAO_SOCIAL");
        cdsEmpresaLogin.open();

        CriaComponentesHtml html = new CriaComponentesHtml();
        html.vsInt = vs;

        sb.append(html.criaGrid(cdsEmpresaLogin, "con_EMPRESALOGIN", "registroInicial=['CEMPRESA','" + vs.getValor("ultimacempresa") + "']", ""));

        sb.append("</div><input class=\"btn-padrao\" id=\"btnCancelarValida\" onclick=\"selecionouEmpresa3(this)\" type=\"button\" value=\"Selecionar\"></input>"
                + " <script>quemsou.querySelector(\"#btnCancelarValida\").focus();"
                + " var bname=navigator.appName;var bversion=navigator.appVersion;"
                + " if(bname == \"Microsoft Internet Explorer\" || bversion.indexOf(\"JavaFX\") > 0) {  if(bversion[0] == 4 || bversion.indexOf(\"JavaFX\") > 0) { jQuery('.btnSelEmpresa').get(0).onkeydown = function() { if(event.keyCode != 13) return; selecionouEmpresa3(this); } }}"
                + "</script></div></div></div><script>addFuncDialog('" + idDialog + "'); ultimozindex = document.querySelector('#" + idDialog + "'); "
                + " zindexJanela++; num++; contador++; tDialog(jQuery('#" + idDialog + "').get(0),document.body,{destroy:true, funcfecha:'',modal:'false' });"
                + "</script></div>");

        return sb.toString();
    }

}
