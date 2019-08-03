/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tecnicon.paginas;

import br.com.tecnicon.criptografia.PWSec;
import br.com.tecnicon.enviaemail.TEnviarEmail;
import br.com.tecnicon.objects.put.DadosEmpresa;
import br.com.tecnicon.police.ListaObjetosXml;
import br.com.tecnicon.server.Propriedades;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.execoes.ExcecaoMsg;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.model.EmailConfig;
import br.com.tecnicon.server.sessao.VariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import br.com.tecnicon.util.EmailBuilder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ejb.Stateless;

/**
 *
 * @author Jacob
 */
@Stateless
public class EsqueciSenha
{

    public void a(VariavelSessao vs)
    {
        ListaObjetosXml.reload();

    }

    private String enviarEmailTrocaSenha(VariavelSessao vs, TClientDataSet usuario, String senha) throws ExcecaoTecnicon
    {
        /*TSQLDataSetTec enderecoWeb = TSQLDataSetTec.create(vs);
        enderecoWeb.commandText("SELECT SISTEMA.ENDERECOWEB FROM SISTEMA ");
        enderecoWeb.open();*/
        
        TClientDataSet enderecoWeb = TClientDataSet.create(vs, "SISTEMA");
        enderecoWeb.createDataSet();
        enderecoWeb.condicao("WHERE SISTEMA.ENDERECOWEB IS NOT NULL");
        enderecoWeb.open();

        String link = enderecoWeb.fieldByName("ENDERECOWEB").asString();
        if (link.endsWith("#"))
        {
            link = link.replace("#", "");
        }

        if (!link.endsWith("/Tecnicon/Portal"))
        {
            if (link.endsWith("/Tecnicon"))
            {
                link = link + "/Portal";
            } else
            {
                if (link.endsWith("/"))
                {
                    link = link + "Tecnicon/Portal";
                } else
                {
                    link = link + "/Tecnicon/Portal";
                }
            }
        }

        String link1 = link;
        String cempresa = "N";
        if (Propriedades.isServidorCloud() || Propriedades.isSemBancoTecnicon())
        {
            cempresa = vs.getParameter("empresa");
        }

        boolean semerro = true;
        try
        {
            link = link.replace("/Portal", "/TROCASENHA") + "?s=" + PWSec.encryptForHttpGet("&senha=" + senha + "&cusuario=" + usuario.fieldByName("CUSUARIO").asString() + "&cempresa=" + cempresa + "&link1=" + link1 + "&valSenha=" + Funcoes.dateToStr(new Date()));
        } catch (BadPaddingException ex)
        {
            semerro = false;
        } catch (NoSuchPaddingException ex)
        {
            semerro = false;
        } catch (IllegalBlockSizeException ex)
        {
            semerro = false;
        } catch (InvalidKeyException ex)
        {
            semerro = false;
        } catch (NoSuchAlgorithmException ex)
        {
            semerro = false;
        } catch (InvalidAlgorithmParameterException ex)
        {
            semerro = false;
        }

        if (!semerro)
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html>");
        sb.append("<html>");

        EmailBuilder eb = new EmailBuilder();
        eb.addText("Prezado(a) " + usuario.fieldByName("NOMECOMPLETO").asString());

        eb.addTable();
        eb.newLine().addCol("Conforme solicitado, segue abaixo uma nova senha para você: ");
        eb.newLine().addCol("<b>" + senha + "</b>");
        eb.newLine().addCol("Clique no link para atualizar sua senha: "
                + "<a href=\"" + link + "\" target=\"_blank\"> " + link1 + "</a>");

        eb.newLine().addCol("<br><br>Caso não tenha solicitado a alteração de senha, favor desconsiderar este Email.");

        eb.closeTable();

        eb.addAssinaturaTec();

        sb.append(eb);

        sb.append("</html>");

        TEnviarEmail enviarEmail = new TEnviarEmail();

        /*TSQLDataSetTec USUARIOEMAIL = TSQLDataSetTec.create(vs);
        USUARIOEMAIL.createDataSet();
        USUARIOEMAIL.commandText("SELECT USUARIOEMAIL.* "
                + " FROM USUARIOEMAIL"
                + " WHERE USUARIOEMAIL.CUSUARIO=" + usuario.fieldByName("CUSUARIO").asString());
        USUARIOEMAIL.open();

        EmailConfig emailConf
                = new EmailConfig(USUARIOEMAIL.fieldByName("USUARIO").asString(),
                        USUARIOEMAIL.fieldByName("SENHA").asString(),
                        USUARIOEMAIL.fieldByName("HOSTSMTP").asString(),
                        USUARIOEMAIL.fieldByName("EMAIL").asString(),
                        USUARIOEMAIL.fieldByName("NOME").asString(),
                        USUARIOEMAIL.fieldByName("ASSINATURA").asString(),
                        USUARIOEMAIL.fieldByName("LOCALIMAGEM").asString(),
                        USUARIOEMAIL.fieldByName("PORTSMTP").asInteger(),
                        USUARIOEMAIL.fieldByName("SSL").asString(),
                        0,
                        0);
        enviarEmail.conectarSMTP(emailConf);*/
        EmailConfig config = new EmailConfig("testedrive@tecnicon.com.br", "t3st3",
                "mail.tecnicon.com.br", "testedrive@tecnicon.com.br", "Tecnicon Business Suite",
                "", "", 587, "S", 0, 0);

        enviarEmail.conectarSMTP(config);
        //EmailConfig config = new EmailConfig("smartcityapp@tecnicon.com.br", "sm@rtc1tyapp!", "mail.tecnicon.com.br", "smartcityapp@tecnicon.com.br", "SmartCity", "", "", 587, "S", 60, 1);
        enviarEmail.enviarEmail(usuario.fieldByName("EMAIL").asString(), "", "", "TECNICON Business Suite - Alteração de Senha", sb.toString(), config, true);

        return "OK";
    }

    public String esqueciSenhaEmail(VariavelSessao vs) throws ExcecaoTecnicon
    {
        if (vs.getParameter("email") == null || "".equals(vs.getParameter("email")))
        {
            throw new ExcecaoMsg(vs, "Informe o E-mail no campo Usuário para continuar");
        }

        if (Propriedades.isServidorCloud())
        {
            if (vs.getParameter("empresaURL") != null && !vs.getParameter("empresaURL").equals(""))
            {
                TClientDataSet empresas = DadosEmpresa.getEmpresas(" RAZAO_SOCIAL = '" + vs.getParameter("empresaURL") + "'");
                if (!empresas.isEmpty())
                {
                    vs.addParametros("empresa", empresas.fieldByName("CEMPRESA").asString());
                } else
                {
                    throw new ExcecaoMsg(vs, "Empresa '" + vs.getParameter("empresaURL") + "' não indentificada");
                }
            }
        }

        TClientDataSet cds = TClientDataSet.create(vs, "USUARIO");
        cds.createDataSet();
        cds.condicao("WHERE USUARIO.EMAIL = '" + vs.getParameter("email") + "'");
        cds.open();

        if (cds.isEmpty())
        {
            throw new ExcecaoMsg(vs, "O e-mail: " + vs.getParameter("email") + ", não pertence a uma conta existente!");
        }

        String novaSenha = gerarNovaSenha();

        enviarEmailTrocaSenha(vs, cds, novaSenha);

        return "OK";
    }

    private String gerarNovaSenha()
    {
        Random r = new Random();
        String pass = "";
        String c = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 5; i++)
        {
            pass += c.charAt(r.nextInt(36));
        }
        pass = c.charAt(r.nextInt(26)) + pass.toLowerCase();
        c = "!@#$%*(),.";
        pass = pass + c.charAt(r.nextInt(11));
        return pass;
    }

}
