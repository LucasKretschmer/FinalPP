package br.com.tecnicon.paginas;

import br.com.tecnicon.control.TTranslate;
import br.com.tecnicon.criptografia.SHA256;
import br.com.tecnicon.server.context.TecniconLookup;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.dataset.TSQLDataSetTec;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.regex.Pattern;
import javax.ejb.Stateless;

/**
 *
 * @author patricia.diniz
 */
@Stateless
public class TecniconGerenciador
{

    public String obterTelaHtml(VariavelSessao request)
    {
        try
        {

            request.addParametros("parametro", "parsql=");
            if (validarSenhaAtual(request, request.getParameter("cusuario"), request.getParameter("senhaAtual")))
            {
                return alterarSenha(request.getParameter("cusuario"), request.getParameter("novaSenha"), request);
            } else
            {
                return "erro:" + TTranslate.translate(request, "Senha atual inválida!");
            }
        } catch (ExcecaoTecnicon ex)
        {

            return "erro:" + ex.getMessage();
        }
    }

    public Boolean validarSenhaAtual(VariavelSessao request, String cusuario, String senha) throws ExcecaoTecnicon
    {

        TClientDataSet sqlemp = TClientDataSet.create(request, "USUARIO");
        sqlemp.createDataSet();
        sqlemp.condicao(" WHERE USUARIO.CUSUARIO=" + cusuario);
        sqlemp.open();
        sqlemp.first();

        String senhaBanco = sqlemp.fieldByName("senha").asString();

        try
        {
            senha = SHA256.encodePswd(request, senha);
        } catch (NoSuchAlgorithmException ex)
        {
            throw new ExcecaoTecnicon(request, ex);
        }

        if (senhaBanco.equals(senha))
        {
            return true;
        } else
        {
            return false;
        }

    }

    public String alterarSenha(String cusuario, String novaSenha, VariavelSessao request)
    {
        try
        {
            TClientDataSet cdsUsuario = TClientDataSet.create(request, "USUARIO");
             cdsUsuario.createDataSet();
            cdsUsuario.condicao("WHERE USUARIO.CUSUARIO=" + cusuario);
            cdsUsuario.open();

            TSQLDataSetTec con = TSQLDataSetTec.create(request);
            con.commandText("SELECT PARAMETROTEC.NUMSENHASHASH FROM PARAMETROTEC");
            con.open();

            if (con.fieldByName("NUMSENHASHASH").asInteger() > 0)
            {
                int nsenhas = con.fieldByName("NUMSENHASHASH").asInteger();

                TClientDataSet SENHASHIST = TClientDataSet.create(request, "SENHASHIST");
                SENHASHIST.createDataSet();
                SENHASHIST.condicao("WHERE SENHASHIST.CUSUARIO = " + request.getValor("cusuario"));
                SENHASHIST.ordenar("ORDER BY SENHASHIST.DATA DESC, SENHASHIST.SSENHASHIST DESC");
                SENHASHIST.open();

                String sAtual = "";
                Object criptografiaSenhaBanco = TecniconLookup.lookup("TecniconSecurity", "CriptografiaSenhaBanco");
                try
                {
                    sAtual = (String) criptografiaSenhaBanco.getClass().getMethod("retornaSHA2", String.class).invoke(criptografiaSenhaBanco, novaSenha);
                } catch (Exception ex)
                {
                    throw new ExcecaoTecnicon(request, ex.getMessage());
                }

                while (!SENHASHIST.eof())
                {
                    if (sAtual == null ? SENHASHIST.fieldByName("HASHSENHA").asString() == null : sAtual.equals(SENHASHIST.fieldByName("HASHSENHA").asString()))
                    {
                        return "erro:A senha escolhida não pode ser a mesma utilizada em uma das " + nsenhas + " senhas anteriores.";
                    }
                    SENHASHIST.next();
                }

                SENHASHIST.insert();
                SENHASHIST.fieldByName("CUSUARIO").asString(request.getValor("cusuario"));
                SENHASHIST.fieldByName("DATA").asDate(new Date());
                SENHASHIST.fieldByName("HASHSENHA").asString(sAtual);
                SENHASHIST.post();

                SENHASHIST.close();
                SENHASHIST.open();
                if (SENHASHIST.recordCount() > nsenhas)
                {
                    SENHASHIST.last();
                    while (!SENHASHIST.bof())
                    {
                        SENHASHIST.delete();
                        SENHASHIST.prior();
                        if (SENHASHIST.recordCount() <= nsenhas)
                        {
                            break;
                        }
                    }
                }
            }

            try
            {
                novaSenha = SHA256.encodePswd(request, novaSenha);
            } catch (NoSuchAlgorithmException ex)
            {
                throw new ExcecaoTecnicon(request, ex);
            }

            cdsUsuario.edit();
            cdsUsuario.fieldByName("SENHA").asString(novaSenha);
            cdsUsuario.fieldByName("SENHADTALT").asDateTime(new Date());
            cdsUsuario.post();

            return TTranslate.translate(request, "Senha Alterada com sucesso!");
        } catch (ExcecaoTecnicon ex)
        {
            return "Ocorreu um erro ao tentar alterar a senha do usuário. Detalhes:" + ex.getMessage();
        }
    }

    public String senhaDentroValidade(VariavelSessao vs) throws ExcecaoTecnicon
    {
        TClientDataSet cdsUsuario = TClientDataSet.create(vs, "USUARIO");
        cdsUsuario.createDataSet();
        cdsUsuario.condicao("WHERE USUARIO.CUSUARIO=" + vs.getValor("cusuario"));
        cdsUsuario.open();

        TSQLDataSetTec con = TSQLDataSetTec.create(vs);
        con.commandText("SELECT PARAMETROTEC.DIASVALSENHA FROM PARAMETROTEC");
        con.open();

        if (cdsUsuario.recordCount() <= 0)
        {
            return "erro:" + TTranslate.translate(vs, "Usuário não localizado");
        }

        if (con.fieldByName("DIASVALSENHA").asInteger() <= 0)
        {
            return "true";
        }

        if (Funcoes.diferencaEmDias(cdsUsuario.fieldByName("SENHADTALT").asDate(), new Date()) > con.fieldByName("DIASVALSENHA").asInteger())
        {
            return "false";
        } else if (con.fieldByName("DIASVALSENHA").asInteger() - Funcoes.diferencaEmDias(cdsUsuario.fieldByName("SENHADTALT").asDate(), new Date()) < 3)
        {
            return 1 + (con.fieldByName("DIASVALSENHA").asInteger() - Funcoes.diferencaEmDias(cdsUsuario.fieldByName("SENHADTALT").asDate(), new Date())) + "";
        }

        return "true";
    }

    public String checaSeguranca(VariavelSessao vs)
    {
        String senha = vs.getParameter("CAMPO");
        int entrada = 0;
        String resultado = "";

        if (senha == null)
        {
            return "";
        }

        try
        {
            TSQLDataSetTec con = TSQLDataSetTec.create(vs);
            con.commandText("SELECT PARAMETROTEC.NUMSENHASHASH FROM PARAMETROTEC");
            con.open();

            if (con.fieldByName("NUMSENHASHASH").asInteger() > 0)
            {
                int nsenhas = con.fieldByName("NUMSENHASHASH").asInteger();

                TClientDataSet SENHASHIST = TClientDataSet.create(vs, "SENHASHIST");
                SENHASHIST.createDataSet();
                SENHASHIST.condicao("WHERE SENHASHIST.CUSUARIO = " + vs.getValor("cusuario"));
                SENHASHIST.ordenar("ORDER BY SENHASHIST.DATA DESC, SENHASHIST.SSENHASHIST DESC");
                SENHASHIST.open();

                String sAtual = "";
                Object criptografiaSenhaBanco = TecniconLookup.lookup("TecniconSecurity", "CriptografiaSenhaBanco");
                try
                {
                    sAtual = (String) criptografiaSenhaBanco.getClass().getMethod("retornaSHA2", String.class).invoke(criptografiaSenhaBanco, senha);
                } catch (Exception ex)
                {
                    throw new ExcecaoTecnicon(vs, ex.getMessage());
                }

                while (!SENHASHIST.eof())
                {
                    if (sAtual == null ? SENHASHIST.fieldByName("HASHSENHA").asString() == null : sAtual.equals(SENHASHIST.fieldByName("HASHSENHA").asString()))
                    {
                        resultado = "A segurança de sua senha é: <font color=\'#A04040\'>IGUAL À SENHAS ANTIGAS</font>";
                        return resultado;
                    }
                    SENHASHIST.next();
                }
            }
        } catch (ExcecaoTecnicon ex)
        {
            //Just keep walking
        }

        if (senha.equals(vs.getParameter("SENHAANTIGA")))
        {
            resultado = "A segurança de sua senha é: <font color=\'#A04040\'>IGUAL A ATUAL</font>";
            return resultado;
        }

        if (Pattern.compile("(.)\\1", Pattern.CASE_INSENSITIVE).matcher(senha).find())
        {
            entrada = entrada - 1;
        }

        if (!Pattern.compile("[a-zA-Z]", Pattern.CASE_INSENSITIVE).matcher(senha).find() || !Pattern.compile("[0-9]", Pattern.CASE_INSENSITIVE).matcher(senha).find())
        {
            entrada = entrada - 1;
        }

        if (!Pattern.compile("[^\\w]", Pattern.CASE_INSENSITIVE).matcher(senha).find())
        {
            entrada = entrada - 1;
        }

        if (senha.length() < 5)
        {
            entrada = entrada - 2;
        } else if (senha.length() < 7)
        {
            entrada = entrada - 1;
        }

        if (entrada == 0)
        {
            resultado = "A segurança de sua senha é: <font color=\'#99C55D\'>EXCELENTE</font>";
        } else if (entrada == -1)
        {
            resultado = "A segurança de sua senha é: <font color=\'#7F7FFF\'>BOA</font>";
        } else if (entrada == -2)
        {
            resultado = "A segurança de sua senha é: <font color=\'#FF5F55\'>BAIXA</font>";
        } else if (entrada == -3 || entrada == -4)
        {
            resultado = "A segurança de sua senha é: <font color=\'#A04040\'>MUITO BAIXA</font>";
        }

        return resultado;
    }
}
