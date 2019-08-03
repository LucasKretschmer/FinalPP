package br.com.tecnicon.paginas;

import br.com.tecnicon.bd.manager.TGerenciaConexao;
import br.com.tecnicon.componente.CriaComponentesHtml;
import br.com.tecnicon.control.TTranslate;
import br.com.tecnicon.controller.EncerarSistema;
import br.com.tecnicon.police.TValidaSessao;
import br.com.tecnicon.server.context.TecniconLookup;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.interfaces.TTela;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.io.Serializable;
import java.lang.reflect.Method;
import javax.ejb.Stateless;

/**
 *
 * @author jean.siqueira
 */

@Stateless
public class Filiais implements TTela, Serializable
{

    @Override
    public String obterTelaHtml(VariavelSessao vs)
    {
        try
        {
            String empresa = vs.getParameter("empresa");
            String nomeEmpresa = vs.getParameter("nomeEmpresa");;

            String eliminarSessao = vs.getParameter("eliminarSessao");

            if (eliminarSessao != null && eliminarSessao.equals("S"))
            {
                vs.addParametros("motivo_encerra", "Sessão encerrada pelo usuário na tela sessões ativas, após o login");
                TValidaSessao.getInstance().excluirSessoesAtivas(vs);
            }

            if (vs.getValor("empresaRef") != null && !vs.getValor("empresaRef").equals(""))
            {
                TGerenciaConexao gerenciar = TGerenciaConexao.getInstance();
                try
                {
                    vs.addParametros("empresa", vs.getValor("empresaRef"));
                    gerenciar.fecharConexaoEmpPorSessao(vs);
                } catch (Exception ex)
                {
                    return "erro:" + ex.getMessage();
                }
            }

            if (empresa != null && !empresa.equals(""))
            {
                vs.addParametros("empresa", empresa);
                vs.addParametros("empresaRef", empresa);
                if (nomeEmpresa != null && !nomeEmpresa.equals(""))
                {
                    vs.addParametros("nomeEmpresa", nomeEmpresa);
                    vs.addParametros("nomeempresa", nomeEmpresa);
                }
                //Para nova entrada precisa ser em minusculo
            }

            try
            {
                Object validacaoHorario = TecniconLookup.lookup("TecniconSecurity", "ValidacaoHorario");
                Method permiteLogin = validacaoHorario.getClass().getMethod("permiteLogin", VariavelSessao.class, Integer.class);
                boolean permite = (Boolean) permiteLogin.invoke(validacaoHorario, vs, Integer.parseInt(vs.getValor("cusuario")));
                if (!permite)
                {
                    try
                    {
                        new EncerarSistema().encerraSessao(vs, "Sessão fora do horário permitido");
                    } catch (Exception ex)
                    {
                    }
                    return "erro:O usuário \"" + vs.getValor("nome") + "\" não possui permissão de acesso ao sistema neste horário!";
                }
            } catch (Exception ex)
            {
                if (ex != null && ex.getMessage() != null && ex.getMessage().contains("TecniconSecurity/ValidacaoHorario não localizado"))
                {
                    return "erro:" + TTranslate.translate(vs, "O sistema esta terminando a atualização. Por favor aguarde mais alguns segundos e tente novamente.");
                }

                throw new ExcecaoTecnicon(vs, ex);
            }

            if (vs.getValor("sessao") == null || vs.getValor("sessao").equals(""))
            {
                throw new ExcecaoTecnicon(vs, "vs.getValor('sessao') nulo ou vazio", new Throwable(), true);
            }
            
            String grupos = Grupos.obterGrupos(vs);

            if (grupos.trim().equals(""))
            {
                new EncerarSistema().encerraSessao(vs, "Usuário da sessão não possuí grupos de usuário configurados");
                return "erro:" + TTranslate.translate(vs, "Favor definir um grupo de usuário para este usuário");
            }
            if (vs.getValor("franquia") != null && vs.getValor("franquia").equals("S"))
            {
                vs.addParametros("franquia", "N");
            }
            TClientDataSet sqlemp = TClientDataSet.create(vs, "FILIAL");
            sqlemp.createDataSet();
            sqlemp.condicao(" WHERE EXISTS (SELECT GRUSUARIOFILIAL.CFILIAL "
                    + " FROM GRUSUARIOFILIAL"
                    + " WHERE GRUSUARIOFILIAL.CFILIAL = FILIAL.CFILIAL"
                    + " AND GRUSUARIOFILIAL.CGRUSUARIO IN ( " + grupos + " ))");
            sqlemp.open();

            if (sqlemp.recordCount() <= 0)
            {
                new EncerarSistema().encerraSessao(vs, "Usuário da sessão não possuí nenhuma filial liberada");
                return "erro:" + TTranslate.translate(vs, "Não há nenhuma filial liberada para o usuário");
            }

            if (sqlemp.recordCount() == 1)
            {
                vs.addParametros("filial", sqlemp.fieldByName("CFILIAL").asString());
                vs.addParametros("nomeFilial", !sqlemp.fieldByName("FANTASIA").asString().equals("") ? sqlemp.fieldByName("FANTASIA").asString() : sqlemp.fieldByName("RAZAO_SOCIAL").asString());
                vs.addParametros("nomefilial", !sqlemp.fieldByName("FANTASIA").asString().equals("") ? sqlemp.fieldByName("FANTASIA").asString() : sqlemp.fieldByName("RAZAO_SOCIAL").asString());

                Locais loc = new Locais();
                return loc.obterTelaHtml(vs);
            } else
            {
                try
                {
                    if (vs.getValor("franquia") != null && vs.getValor("franquia").equals("S"))
                    {
                        vs.addParametros("franquia", "N");
                    }
                    return telaSelecionaFilial(vs);
                } catch (Exception ex)
                {
                    return "erro:" + ex.getMessage();
                }
            }

        } catch (ExcecaoTecnicon ex)
        {
            return "erro:" + ex.getMessage();
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

            if (!cds.fieldByName("CFILIAL").asString().equals(""))
            {
                return cds.fieldByName("CFILIAL").asString();
            }

            return "";
        } catch (ExcecaoTecnicon ex)
        {
            return "";
        }
    }

    private String telaSelecionaFilial(VariavelSessao vs) throws ExcecaoTecnicon
    {
        final String idDialog = vs.getParameter("iddialog");
        final String grupos = Grupos.obterGrupos(vs);

        StringBuilder sb = new StringBuilder(1024);
        sb.append("<div role=\"dialog\" painel=\"false\" id=\"").append(idDialog).append("\" class=\"div-fundo\" modal=\"false\" ttipo=\"TelaDados\" centralizartela=\"false\" style=\"top:0px;max-width:800px;left:0px;width:100%;height:325px;\">\n"
                + "    <div class=\"div-header-dialog gradiente-color\">\n"
                + "        <label caption=\"Selecione a Filial\" class=\"label-dialog\">Selecione a Filial</label></div>"
                + "     <div class=\"div-ancora\" id=\"div-ancora\" tid=\"div-ancora\"><div class=\"div-dados-extended\" dadosjs=\"true\" id=\"div0\"  tid=\"div0\"><div  ttipo=\"tFiliais\" tipo=\"H\" p_ultimacfilial=\"\" id=\"tFiliais0\" class=\"\"   style=\"max-width:800px;width:100%;\" >\n"
                + "    <script>function manFoco(){  quemsou = document.querySelector('#" + idDialog + "'); } var quemsou = document.querySelector('#" + idDialog + "'); jQuery(quemsou).find(\"#btnCancelarValida\").focus();</script><div style=\"height: 200px; position: relative; top: 0; width: 100%; -webkit-box-sizing: border-box; -moz-box-sizing: border-box; box-sizing: border-box;\">");

        TClientDataSet cdsFilial = TClientDataSet.create(vs, "FILIALLOGIN");
        cdsFilial.createDataSet();
        cdsFilial.condicao("WHERE FILIAL.CFILIAL IN (SELECT GRUSUARIOFILIAL.CFILIAL FROM GRUSUARIOFILIAL WHERE GRUSUARIOFILIAL.CGRUSUARIO IN ( " + grupos + " ))");
        cdsFilial.open();

        CriaComponentesHtml html = new CriaComponentesHtml();
        html.vsInt = vs;

        sb.append(html.criaGrid(cdsFilial, "con_FILIALLOGIN", "camposVisiveis=CFILIAL,RAZAO_SOCIAL,FANTASIA,SIGLA;registroInicial=['CFILIAL','" + vs.getValor("ultimacfilial") + "']", ""));

        sb.append("</div><input caption=\"Selecionar\" class=\"btn-padrao\" id=\"btnCancelarValida\" onclick=\"selecionouFilial2(this)\" type=\"button\" value=\"Selecionar\"></input>"
                + "<script>var bname=navigator.appName; var bversion=navigator.appVersion;"
                + " if(bname == \"Microsoft Internet Explorer\" || bversion.indexOf(\"JavaFX\") > 0) if(bversion[0] == 4 || bversion.indexOf(\"JavaFX\") > 0) { jQuery('.btnSelFilial').get(0).onkeydown = function() {if(event.keyCode != 13) return; selecionouFilial2(this); } }</script>"
                + " </div></div></div>"
                + " <script>addFuncDialog('").append(idDialog).append("'); ultimozindex = document.querySelector('#").append(idDialog).append("'); zindexJanela++; num++; contador++; tDialog(jQuery('#").append(idDialog).append("').get(0),document.body,{destroy:true, funcfecha:'',modal:'false' });</script></div>");

        return sb.toString();
    }
}
