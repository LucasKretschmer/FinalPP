package contato;

import br.com.tecnicon.enviaemail.TEnviarEmail;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.model.EmailConfig;
import br.com.tecnicon.server.sessao.VariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.ejb.Stateless;

/**
 *
 * @author Lucas Kretschmer
 */
@Stateless
public class contato {

    public String contato(VariavelSessao vs) throws ExcecaoTecnicon {
        Funcoes.validaVSNNNome(vs, new String[]{
            "EMAIL", "Email"
        }, new String[]{
            "NOME", "Nome"
        }, new String[]{
            "IDADE", "Idade"
        }, new String[]{
            "COMENTARIO", "Comentario"
        });

        TClientDataSet cds = TClientDataSet.create(vs, "SAC");
        cds.createDataSet();
        cds.insert();
        cds.fieldByName("FILIALCF").asInteger(1);
        cds.fieldByName("CSACFORMACONTATO").asInteger(1);
        cds.fieldByName("HRABERTURA").asTime(new Date());
        cds.fieldByName("DTABERTURA").asTime(new Date());
        cds.fieldByName("SACFORMACONTATO").asInteger(1);
        cds.fieldByName("CSACTIPO").asInteger(1);
        cds.fieldByName("CSACCATCONTATO").asInteger(1);
        cds.fieldByName("NOME").asString(vs.getParameter("NOME"));
        cds.fieldByName("EMAIL").asString(vs.getParameter("EMAIL"));
        cds.fieldByName("PROBLEMA").asString(vs.getParameter("COMENTARIO"));
        cds.post();

        return "Comentario enviado com sucesso!";
    }

    public String recuperarSenha(VariavelSessao vs) throws ExcecaoTecnicon {
        Funcoes.validaVSNNNome(vs, new String[]{
            "EMAIL", "Email"
        }, new String[]{
            "CPF", "CPF"
        });

        TClientDataSet cds = TClientDataSet.create(vs, "GSACESSO");
        cds.createDataSet();
        cds.condicao(" WHERE GSACESSO.EMAIL = " + vs.getParameter("EMAIL"));
        cds.open();

        TClientDataSet cds1 = TClientDataSet.create(vs, "CLIFOREND");
        cds1.createDataSet();
        cds1.condicao(" WHERE CLIFOREND.CGC = " + vs.getParameter("CPF"));
        cds1.open();

        if (!cds.isEmpty() && !cds1.isEmpty()) {
            int cod = cds1.fieldByName("CCLIFOR").asInteger();
            String senha = "pass";
            String email;

            TClientDataSet cds2 = TClientDataSet.create(vs, "GSACESSO");
            cds2.createDataSet();
            cds2.condicao(" WHERE GSACESSO.CCLIFOR = " + cod);
            cds2.open();

            Random gerador = new Random();
            senha += gerador.nextInt(99999);

            email = "Utilize a senha - " + senha + " - para fazer loguin em sua conta. \r\n \r\n Att. CampoNovo";

            envioEmail(vs, "Recuperação de Senha", email, vs.getParameter("EMAIL"), "Verifique seu email com a nova senha definida!");

            cds2.edit();
            cds2.fieldByName("SENHA").asString(senha);
            cds2.fieldByName("CONFSENHA").asString(senha);
            cds2.post();

            return "Verifique seu email com a nova senha definida!";
        }
        return "Email ou CPF informados está incorreto!";
    }

    public boolean alteraSenha(VariavelSessao vs) throws ExcecaoTecnicon {
        Funcoes.validaVSNNNome(vs, new String[]{
            "EMAIL", "Email"
        }, new String[]{
            "SENHA", "Senha"
        }, new String[]{
            "CONFSENHA", "Confirmação da Senha"
        }, new String[]{
            "COD", "Codigo do Cliente"
        });

        if (vs.getParameter("SENHA").equals(vs.getParameter("CONFSENHA"))) {
            TClientDataSet cds = TClientDataSet.create(vs, "GSACESSO");
            cds.createDataSet();
            cds.condicao("WHERE GSACESSO.CCLIFOR = " + vs.getParameter("COD"));
            cds.open();
            cds.first();

            cds.edit();
            cds.fieldByName("EMAIL").asString(vs.getParameter("EMAIL"));
            cds.fieldByName("SENHA").asString(vs.getParameter("SENHA"));
            cds.fieldByName("CONFSENHA").asString(vs.getParameter("CONFSENHA"));
            cds.post();

            envioEmail(vs, "Recuperação de Senha", "Sua senha foi alterada!", vs.getParameter("EMAIL"), "Senha redefinida com Sucesso! \n Verifique seu E-Mail!");

            return true;
        }
        return false;
    }

    public void envioEmail(VariavelSessao vs, String titulo, String mensagem, String destinatario, String msgRetorno) throws ExcecaoTecnicon {
        TClientDataSet usuarioEmail = TClientDataSet.create(vs, "USUARIOEMAIL");
        usuarioEmail.createDataSet();
        usuarioEmail.condicao("WHERE USUARIOEMAIL.CUSUARIO = 27 AND USUARIOEMAIL.SUSUARIOEMAIL = 8"); //Setado fixo pois nao temos de onde pegar
        usuarioEmail.open();
        StringBuilder textAdd = new StringBuilder();
        textAdd.append(" \n Att. CampoNovo \n");
        textAdd.append("<img class=\"img-logo\" id=\"index\"   style=\"width: 210px;\" src=\"http://portal.tecnicon.com.br:7078/GSLKWeb/img/logomarca.png\">");
        String content = mensagem + textAdd.toString();
        Map<String, byte[]> anexos = new HashMap<>();
        Map<String, String> inlineImages = new HashMap<>();
        EmailConfig config = new EmailConfig(usuarioEmail.fieldByName("USUARIO").asString(), usuarioEmail.fieldByName("SENHA").asString(),
                usuarioEmail.fieldByName("HOSTSMTP").asString(), usuarioEmail.fieldByName("EMAIL").asString(),
                usuarioEmail.fieldByName("NOME").asString(), "", "", usuarioEmail.fieldByName("PORTSMTP").asInteger(),
                usuarioEmail.fieldByName("SSL").asString(), 17, 0);
        TEnviarEmail email = new TEnviarEmail();
        email.enviarEmail(destinatario,
                "",
                "",
                titulo,
                content,
                config,
                anexos,
                false,
                inlineImages);
        vs.setRetornoOK(msgRetorno);
    }
}
