package login;

import br.com.tecnicon.control.TTranslate;
import br.com.tecnicon.controller.EncerarSistema;
import br.com.tecnicon.criptografia.SHA256;
import br.com.tecnicon.enviaemail.TEnviarEmail;
import br.com.tecnicon.objects.put.DadosEmpresa;
import br.com.tecnicon.paginas.ControlLogin;
import br.com.tecnicon.police.JWTTec;
import br.com.tecnicon.police.ListaCliForEndUser;
import br.com.tecnicon.police.ListaSessoes;
import br.com.tecnicon.police.ListaUsuarios;
import br.com.tecnicon.server.Propriedades;
import br.com.tecnicon.server.bd.entity.CliForEndUser;
import br.com.tecnicon.server.bd.entity.Sessao;
import br.com.tecnicon.server.context.TClassLoader;
import br.com.tecnicon.server.context.TecniconLookup;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.dataset.TSQLDataSetEmp;
import br.com.tecnicon.server.dataset.TSQLDataSetSuporte;
import br.com.tecnicon.server.dataset.TSQLDataSetTec;
import br.com.tecnicon.server.execoes.ExcecaoMsg;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.model.EmailConfig;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import br.com.tecnicon.server.sessao.VariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import br.com.tecnicon.websocket.TWebSocketSingleInte;
import br.com.tecnicon.websocket.TWebSocketVOInte;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.Stateless;
import org.json.JSONObject;

/**
 *
 * @author Lucas Kretschmer
 */
@Stateless
public class login
{

    public String obterTelaHtml(VariavelSessao vs)
    {
        try
        {
            return login(vs);
        } catch (Exception ex)
        {
            return "erro:" + ex.getMessage();
        }
    }

    public void facial(VariavelSessao vs) throws ExcecaoTecnicon
    {
        try
        {
            TSQLDataSetTec loguin = TSQLDataSetTec.create(vs);
            loguin.commandText("SELECT USUARIO.NOME, USUARIO.SENHA FROM USUARIO WHERE USUARIO.CUSUARIO=" + vs.getValor("facial"));
            loguin.open();
            if (!loguin.isEmpty())
            {
                vs.addParametros("usuario", loguin.fieldByName("NOME").asString());
                vs.addParametros("senha", loguin.fieldByName("SENHA").asString());
            }
        } catch (ExcecaoTecnicon ex)
        {
            throw new ExcecaoTecnicon(vs, "Erro: " + ex.getMessage());
        }
    }

    public String login(VariavelSessao vs) throws NoSuchAlgorithmException
    {
        try
        {
            String sessaoAtual = vs.getValor("sessao");
            ListaUsuarios listaUs = ListaUsuarios.getInstance();
            ListaSessoes listaSessoes = ListaSessoes.getInstance();
            String username = "";
            String password = "";
            if (!"".equals(vs.getParameter("facial")) && vs.getParameter("facial") != null)
            {
                facial(vs);
                username = vs.getValor("usuario");
                password = vs.getValor("senha");
                vs.addParametros("ENCODE", "N");
            } else
            {
                username = vs.getParameter("usuario");
                password = vs.getParameter("senha");
                vs.addParametros("ENCODE", "S");
            }
            String cnpjCli = vs.getParameter("cnpj");
            if ("S".equals(vs.getValor("ENCODE")) && password != null && !"cliente".equals(vs.getParameter("tipologin")) && !"fornecedor".equals(vs.getParameter("tipologin")) && !"colaborador".equals(vs.getParameter("tipologin")))
            {
                password = SHA256.encodePswd(vs, password);
            }

            boolean trocouUSuario = false;

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
                        throw new ExcecaoTecnicon(vs, "Empresa '" + vs.getParameter("empresaURL") + "' não indentificada");
                    }
                }

                if (username == null || username.equals(""))
                {
                    username = vs.getValor("usuario");
                }

                if (password == null || password.equals(""))
                {
                    password = vs.getValor("senha");
                }
            }

            if ((username != null && !"".equals(username)) && (password != null && !"".equals(password)))
            {
                CliForEndUser cfeu = null;
                Object criptografiaSenhaBanco = TecniconLookup.lookup("TecniconSecurity", "CriptografiaSenhaBanco");
                //<editor-fold defaultstate="collapsed" desc="Carrega logins dos demais tipos de acessos">
                boolean senhaValida;
                if ("cliente".equals(vs.getParameter("tipologin")) || "fornecedor".equals(vs.getParameter("tipologin")))
                {
                    ListaCliForEndUser listaCli = ListaCliForEndUser.getInstance();

                    if (!listaCli.processouCliforEndUser())
                    {
                        throw new ExcecaoTecnicon(vs, "O sistema está carregando o portal do cliente/fornecedor.");
                    }

                    cfeu = listaCli.obterCliforEndUser(username, cnpjCli);

                    if (cfeu == null || cfeu.getSenha() == null || cfeu.getSenha().equals(""))
                    {
                        return "erro:" + TTranslate.translate(vs, "Usuário,CNPJ ou Senha inválidos!");
                    }

                    try
                    {
                        senhaValida = (Boolean) criptografiaSenhaBanco.getClass().getMethod("verificarSenha", String.class, String.class)
                                .invoke(criptografiaSenhaBanco, password, cfeu.getSenha());
                    } catch (Exception ex)
                    {
                        return "erro:" + ex.getMessage();
                    }

                    if (!senhaValida)
                    {
                        return "erro:" + TTranslate.translate(vs, "Usuário, CNPJ ou Senha inválidos!");
                    }

                    if (cnpjCli != null && "".equals(cnpjCli))
                    {
                        return "erro:" + TTranslate.translate(vs, "Por favor preencha o CNPJ");
                    }

                    cfeu.setCgc(cnpjCli);
                    listaCli.veirificarCNPJ(cfeu);

                    if (cfeu.getNomeFilial() == null)
                    {
                        return "erro:" + TTranslate.translate(vs, "Usuário,CNPJ ou Senha inválidos!");
                    }

                    if ("cliente".equals(vs.getParameter("tipologin")))
                    {
                        vs.addParametros("tipologin", "1");
                    } else if ("fornecedor".equals(vs.getParameter("tipologin")))
                    {
                        vs.addParametros("tipologin", "0");
                    }

                    vs.addParametros("scliforenduser", "" + cfeu.getsCliforEndUser());

                    if (cfeu.getCcliforLogado() == 0)
                    {
                        vs.addParametros("cliforenduser", "0");
                    } else
                    {
                        vs.addParametros("cliforenduser", "" + cfeu.getCcliforLogado());
                    }

                    if (cfeu.getFilialCFLogado() == 0)
                    {
                        vs.addParametros("filialcfuser", "0");
                    } else
                    {
                        vs.addParametros("filialcfuser", "" + cfeu.getFilialCFLogado());
                    }

                    vs.addParametros("nomefilialuser", "" + cfeu.getNomeFilial());
                    vs.addParametros("emaillogin", "" + cfeu.getEmail());
                    vs.addParametros("cempresacliente", "" + cfeu.getEmpsToString());

                    username = cfeu.getUsuario().nome;
                    password = cfeu.getUsuario().senha;

                } else if ("representante".equals(vs.getParameter("tipologin")))
                {
                    vs.addParametros("tipologin", "2");
                    vs.addParametros("cliforenduser", "0");
                    vs.addParametros("filialcfuser", "0");
                } else if ("colaborador".equals(vs.getParameter("tipologin")))
                {
                    if (vs.getParameter("empresa") == null || vs.getParameter("empresa").equals(""))
                    {
                        return "erro:" + TTranslate.translate(vs, "Favor preencha a empresa!");
                    }

                    vs.addParametros("empresa", vs.getParameter("empresa"));
                    vs.addParametros("cempresacliente", "" + vs.getParameter("empresa"));

                    TSQLDataSetEmp buscaDados = TSQLDataSetEmp.create(vs);
                    buscaDados.commandText("SELECT FUNCFOLHAUSUARIO.SFUNCFOLHAUSUARIO, FUNCFOLHAUSUARIO.CFUNC, FUNCFOLHA.EMAIL,"
                            + " FUNCFOLHAUSUARIO.CUSUARIO, FUNCFOLHAUSUARIO.SENHA, FUNCFOLHAUSUARIO.SENHA"
                            + " FROM FUNCFOLHAUSUARIO"
                            + " LEFT JOIN FUNCFOLHA ON (FUNCFOLHA.CFUNC = FUNCFOLHAUSUARIO.CFUNC)"
                            + " WHERE FUNCFOLHA.EMAIL = '" + username + "'");
                    buscaDados.open();

                    if (buscaDados.isEmpty())
                    {
                        return "erro:" + TTranslate.translate(vs, "Usuário ou Senha inválidos!");
                    } else
                    {
                        try
                        {
                            senhaValida = (Boolean) criptografiaSenhaBanco.getClass().getMethod("verificarSenha", String.class, String.class)
                                    .invoke(criptografiaSenhaBanco, password, buscaDados.fieldByName("SENHA").asString());
                        } catch (Exception ex)
                        {
                            return "erro:" + ex.getMessage();
                        }
                        if (senhaValida)
                        {
                            TSQLDataSetTec buscaSenha = TSQLDataSetTec.create(vs);
                            buscaSenha.commandText("SELECT USUARIO.SENHA"
                                    + " FROM USUARIO"
                                    + " WHERE USUARIO.CUSUARIO = " + buscaDados.fieldByName("CUSUARIO").asString());
                            buscaSenha.open();
                            password = buscaSenha.fieldByName("SENHA").asString();
                        }
                        vs.addParametros("tipologin", "3");
                        vs.addParametros("cliforenduser", "0");
                        vs.addParametros("filialcfuser", "0");
                    }
                } else if ("perfilnegocio".equals(vs.getParameter("tipologin")))
                {
                    vs.addParametros("tipologin", "8"); // perfilnegocio
                    vs.addParametros("slug", vs.getParameter("empresaURL"));
                    vs.addParametros("cliforenduser", "0");
                    vs.addParametros("filialcfuser", "0");
                } else if (vs.getParameter("MOBILE") != null && vs.getParameter("MOBILE").equals("S"))
                {
                    if (vs.getParameter("empresa") != null)
                    {
                        vs.addParametros("empresa", vs.getParameter("empresa"));
                    }
                } else if (vs.getParameter("ecommerce") != null && vs.getParameter("ecommerce").equals("S"))
                {
                    if (vs.getParameter("empresa") != null)
                    {
                        vs.addParametros("empresa", vs.getParameter("empresa"));
                    }
                } else
                {
                    vs.addParametros("cliforenduser", "0");
                    vs.addParametros("filialcfuser", "0");
                }
                //</editor-fold>

                String sessao = null;
                int cuser = listaUs.getCodUsuarioByName(vs, username);

                if (cuser != 0)
                {
                    if (listaUs.containsUsuarioBloqueado(vs, cuser) && listaUs.isUsuarioBloqueado(vs, cuser))
                    {
                        if (Funcoes.diferencaEmHoras(listaUs.getDataUsuarioErro(vs, cuser), new Date()) >= 1.0)
                        {
                            listaUs.removeUsuarioBloqueado(vs, cuser);
                            listaUs.setUsuarioErros(vs, cuser + "", 0);
                        } else
                        {
                            return "erro:Você excedeu o número de tentativas de login. Sua conta ficará bloqueada por " + ListaUsuarios.getHorasBloc(vs)
                                    + " horas.<br>Entre em contato com o administrador do sistema";
                        }
                    }
                }

                boolean isUserAd = validaUserAD(vs, username, vs.getParameter("senha")/*password*/);
                boolean pswdTec = listaUs.getSenhaByName(vs, username, password);

                if (!listaUs.getUsuarioByName(vs, username).equals(""))
                {
                    if (pswdTec || isUserAd)
                    {
                        try
                        {
                            if (vs.getValor("usuario").equals("" + listaUs.getNomeByCod(vs, "" + listaUs.getCodUsuarioByName(vs, username))))
                            {
                                trocouUSuario = false;
                            } else
                            {
                                trocouUSuario = true;
                            }

                            vs.addParametros("cusuario", "" + listaUs.getCodUsuarioByName(vs, username));
                            /*
                             *jean: ADICIONADO TESTE DE REPLACE
                             */
                            //String nome = listaUs.getNomeByCod("" + listaUs.getCodUsuario(username)).replace(".", "_");
                            String nome = listaUs.getNomeByCod(vs, "" + listaUs.getCodUsuarioByName(vs, username));

                            vs.addParametros("nome", nome);
                            vs.addParametros("usuario", nome);

                            vs.addParametros("nomecompleto", listaUs.getUsuarioByName(vs, nome));

                            vs.addParametros("email", username);

                            vs.addParametros("senha", listaUs.getSenhaByCodigo(vs, "" + listaUs.getCodUsuarioByName(vs, username)));

                            vs.addParametros("ultimacempresa", ""
                                    + listaUs.getCempresaUsuario(vs, "" + listaUs.getCodUsuarioByName(vs, username)));

                            vs.addParametros("ultimacfilial", ""
                                    + listaUs.getCfilialUsuario(vs, "" + listaUs.getCodUsuarioByName(vs, username)));
                            //request.getSession().setAttribute("request", request);

                            try
                            {
                                if (Propriedades.isServidorTecnicon() || Propriedades.isMaquinaProgramador())
                                {
                                    TSQLDataSetSuporte sup = TSQLDataSetSuporte.create(vs);
                                    sup.commandText("SELECT SUPUSERUSUARIO.NOMESUPORTE"
                                            + " FROM SUPUSERUSUARIO"
                                            + " WHERE SUPUSERUSUARIO.CUSUARIO=" + vs.getValor("cusuario"));
                                    sup.open();
                                    if (!sup.isEmpty())
                                    {
                                        vs.addParametros("usuariosuporte", sup.fieldByName("NOMESUPORTE").asString());
                                        vs.addParametros("NOMESUPORTE", sup.fieldByName("NOMESUPORTE").asString());
                                    }
                                }
                            } catch (Exception e)
                            {
                                //quando não consegui conectar no suporte pode seguir o login normalmente
                            }

                            try
                            {
                                sessao = listaSessoes.criarSessao(vs);

                                if (sessao == null)
                                {
                                    throw new ExcecaoTecnicon(vs, "Sessão não criada");
                                }
                            } catch (Exception e)
                            {
                                return "erro:" + TTranslate.translate(vs, "Não foi possível criar uma sessão");
                            }

                            br.com.tecnicon.police.Util.getSessaoEmp(vs, sessao).antesLogin = true;

                        } catch (ExcecaoTecnicon ex)
                        {
                            return "erro:" + ex.getMessage();
                        }

                    } else
                    {
                        return trataUserSenInvalidos(listaUs, username, vs);
                    }

                } else if (!listaUs.getUsuario(vs, username).equals(""))
                {
                    if (listaUs.getSenha(vs, username, password))
                    {
                        vs.addParametros("cusuario", "" + listaUs.getCodUsuario(vs, username));
                        /*
                         * ADICIONADO TESTE DE REPLACE
                         */
                        //String nome = listaUs.getNomeByCod("" + listaUs.getCodUsuario(username)).replace(".", "_");
                        String nome = listaUs.getNomeByCod(vs, "" + listaUs.getCodUsuario(vs, username));
                        vs.addParametros("nome", nome);
                        vs.addParametros("usuario", nome);
                        vs.addParametros("nomecompleto", listaUs.getUsuarioByName(vs, nome));
                        vs.addParametros("email", username);
                        vs.addParametros("senha", listaUs.getSenhaByCodigo(vs, "" + listaUs.getCodUsuario(vs, username)));
                        vs.addParametros("ultimacempresa", "" + listaUs.getCempresaUsuario(vs, "" + listaUs.getCodUsuario(vs, username)));
                        vs.addParametros("ultimacfilial", "" + listaUs.getCfilialUsuario(vs, "" + listaUs.getCodUsuario(vs, username)));
                        //request.getSession().setAttribute("request", request);
                        sessao = listaSessoes.criarSessao(vs);

                        if (sessao == null)
                        {
                            return "erro:" + TTranslate.translate(vs, "Não foi possível criar uma sessão");
                        }

                        br.com.tecnicon.police.Util.getSessaoEmp(vs, sessao).antesLogin = true;

                    } else
                    {
                        return trataUserSenInvalidos(listaUs, username, vs);
                    }
                } else
                {
                    return "erro:" + TTranslate.translate(vs, "Usuário ou senha inválidos!");
                }

                String retornoLoginExterno = "";
                if (sessao != null)
                {
                    if (vs.getParameter("MOBILE") != null && vs.getParameter("MOBILE").equals("S"))
                    {
                        HashMap<String, String> js = new HashMap<>();
                        js.put("CUSUARIO", vs.getValor("cusuario"));
                        js.put("USUARIO", vs.getValor("usuario"));
                        js.put("SENHA", vs.getParameter("senha"));
                        if ("true".equals(vs.getValor("USA_JWT")))
                        {
                            js.put("SESSAO", JWTTec.create(vs.getValor("sessao")));
                        } else
                        {
                            js.put("SESSAO", vs.getValor("sessao"));
                        }
                        js.put("empresa", vs.getValor("empresa"));

                        if (vs.getParameter("filial") != null)
                        {
                            vs.addParametros("filial", vs.getParameter("filial"));
                        }

                        br.com.tecnicon.server.bd.entity.Sessao s = br.com.tecnicon.police.Util.getSessaoEmp(vs, vs.getValor("sessao"));
                        s.tipo = "M";
                        s.antesLogin = false;
                        vs.addParametros("tipologin", "M");

                        TClassLoader.execMethod("TecniconPadroesVS/TecniconPadroesVS", "preenchePadroesVS", vs);

                        retornoLoginExterno = Funcoes.MapToJson(js);
                    }

                    if (vs.getParameter("ecommerce") != null && vs.getParameter("ecommerce").equals("S"))
                    {
                        HashMap<String, String> js = new HashMap<>();
                        js.put("CUSUARIO", vs.getValor("cusuario"));
                        js.put("USUARIO", vs.getValor("usuario"));
                        js.put("SENHA", vs.getValor("senha"));
                        if ("true".equals(vs.getValor("USA_JWT")))
                        {
                            js.put("SESSAO", JWTTec.create(vs.getValor("sessao")));
                        } else
                        {
                            js.put("SESSAO", vs.getValor("sessao"));
                        }
                        js.put("empresa", vs.getValor("empresa"));
                        js.put("cliforenduser", vs.getValor("cliforenduser"));
                        js.put("filialcfuser", vs.getValor("filialcfuser"));
                        js.put("nomefilialuser", vs.getValor("nomefilialuser"));

                        br.com.tecnicon.server.bd.entity.Sessao s = br.com.tecnicon.police.Util.getSessaoEmp(vs, vs.getValor("sessao"));
                        s.tipo = "E";
                        vs.addParametros("tipologin", "E");

                        TClassLoader.execMethod("TecniconPadroesVS/TecniconPadroesVS", "preenchePadroesVS", vs);

                        return Funcoes.MapToJson(js);
                    }

                    Sessao sess = new Sessao();

                    Map<String, String[]> parameterMap = new HashMap<String, String[]>();

                    for (String key : vs.getParameterMap().keySet())
                    {
                        parameterMap.put(key, new String[]
                        {
                            vs.getParameter(key)
                        });
                    }

                    // 334 ==> objeto TelaEmpresa
                    parameterMap.put("carregouJSs", new String[]
                    {
                        "true"
                    });

                    vs.setParameterMap(parameterMap);

                    if (!sessaoAtual.equals("-9876"))
                    {
                        VariavelSessao vs1 = new TVariavelSessao();
                        vs1.addParametros("sessao", sessaoAtual);
                        vs1.addParametros("cusuario", vs.getValor("cusuario"));
                        if (!vs.getValor("empresa").isEmpty())
                        {
                            vs1.addParametros("empresa", vs.getValor("empresa"));
                        }
                        new EncerarSistema().encerraSessao(vs1, "Sessão " + sessaoAtual + " encerrada ao criar sessão " + vs.getValor("sessao"));
                    }

                    String retorno = "";
                    if (!trocouUSuario && "true".equals(vs.getParameter("logintimeout")))
                    {
                        String ret = ControlLogin.ValidaQtdeSessoes(vs, true, 2);

                        if (ret.startsWith("erro:"))
                        {
                            return ret;
                        }

                        br.com.tecnicon.police.Util.getSessaoEmp(vs, vs.getValor("sessao")).antesLogin = false;

                        TWebSocketVOInte bkpSock = TWebSocketSingleInte.getInstance().obterWebSocketSessao(sessaoAtual);
                        if (bkpSock != null && bkpSock.getAtivo().equals("S"))
                        {
                            TWebSocketSingleInte.getInstance().removeWebSocket(sessaoAtual);
                            TWebSocketSingleInte.getInstance().addWebSocket(sessao, bkpSock);
                        }

                        if ("true".equals(vs.getValor("USA_JWT")))
                        {
                            sessao = JWTTec.create(sessao);
                        }

                        retorno = "" + sessao;

                    } else if (vs.getParameter("MOBILE") != null && vs.getParameter("MOBILE").equals("S"))
                    {
                        String ret = ControlLogin.ValidaQtdeSessoes(vs, true, 2);

                        if (ret.startsWith("erro:"))
                        {
                            return ret;
                        }

                        retorno = retornoLoginExterno;
                    }

                    return retorno;

                } else if (vs.getRetornoErro() != null)
                {
                    return "erro:Não foi possivel criar a sessão. \nDetalhes:\n" + vs.getRetornoErro();
                } else
                {
                    return "erro:Não foi possivel criar a sessão.";
                }
            } else
            {
                return "erro:" + TTranslate.translate(vs, "Usuário ou senha não preeenchidos!");
            }
        } catch (ExcecaoTecnicon ex)
        {
            return "erro:" + ex.getMessage();
        }
    }

    private boolean validaUserAD(VariavelSessao vs, String username, String password) throws ExcecaoTecnicon
    {
        Object activeDirectory = TecniconLookup.lookup("TecniconSecurity", "ActiveDirectory");
        try
        {
            return (Boolean) activeDirectory.getClass().getMethod("authenticateAD", VariavelSessao.class, String.class, String.class).invoke(activeDirectory, vs, username, password);
        } catch (Exception e)
        {
            return false;
        }
    }

    private String trataUserSenInvalidos(ListaUsuarios listaUs, String username, VariavelSessao vs) throws ExcecaoTecnicon
    {
        int err = 0;
        int cuser = 0;

        cuser = listaUs.getCodUsuarioByName(vs, username);

        if (listaUs.containsUsuarioErros(vs, cuser + ""))
        {
            err = listaUs.getUsuarioErros(vs, cuser + "");
        }

        if (listaUs.containsDataUsuarioErro(vs, cuser))
        {
            if (Funcoes.diferencaEmHoras(listaUs.getDataUsuarioErro(vs, cuser), new Date()) >= 1.0)
            {
                err = 0;
            }
        }

        err++;
        listaUs.setUsuarioErros(vs, cuser + "", err);
        listaUs.setDataUsuarioErro(vs, cuser, new Date());

        if (err > listaUs.getQtdeErrosEmail(vs) && listaUs.getQtdeErrosEmail(vs) > 0)
        {
            enviaEmail(cuser, vs);

            listaUs.setUsuarioBloqueado(vs, cuser, true);

            return "erro:Você excedeu o número de tentativas de login. Sua conta ficará bloqueada por " + ListaUsuarios.getHorasBloc(vs)
                    + " horas.<br>Entre em contato com o administrador do sistema";
        }

        return "erro:" + TTranslate.translate(vs, "Usuário ou senha inválidos!");
    }

    private void enviaEmail(int cuser, VariavelSessao vs)
    {
        try
        {
            TClientDataSet cdsEmail = TClientDataSet.create(vs, "USUARIOEMAIL");
            cdsEmail.createDataSet();
            cdsEmail.condicao("WHERE USUARIOEMAIL.CUSUARIO = (SELECT USUARIO.CUSUARIO FROM USUARIO WHERE USUARIO.NOME ='SUPERVISOR')");
            cdsEmail.open();

            if (cdsEmail.recordCount() > 0)
            {
                EmailConfig config = new EmailConfig(cdsEmail.fieldByName("USUARIO").asString(), cdsEmail.fieldByName("SENHA").asString(),
                        cdsEmail.fieldByName("HOSTSMTP").asString(), cdsEmail.fieldByName("EMAIL").asString(), cdsEmail.fieldByName("NOME").asString(),
                        cdsEmail.fieldByName("ASSINATURA").asString(), cdsEmail.fieldByName("LOCALIMAGEM").asString(), cdsEmail.fieldByName("PORTSMTP").asInteger(),
                        cdsEmail.fieldByName("SSL").asString(), 60, 1);

                String assunto = TTranslate.translate(vs, "Usuário com login bloqueado");
                String conteudo = "O usuário " + ListaUsuarios.getInstance().getNomeByCod(cuser + "") + " teve o seu login bloqueado por ultrapassar o limite de erros ao tentar logar.";

                TEnviarEmail email = new TEnviarEmail();
                email.conectarSMTP(config);
                email.enviarEmail(cdsEmail.fieldByName("EMAIL").asString(), "", "", assunto, conteudo, config, true);
            }
        } catch (ExcecaoTecnicon ex)
        {

        }
    }

    public String carregaEmpresa(VariavelSessao vs, String empresa) throws Exception
    {
        TClientDataSet empresas = DadosEmpresa.getEmpresas(" RAZAO_SOCIAL = '" + empresa + "'");
        if (empresas.isEmpty())
        {
            throw new Exception("Empresa " + empresa + " não localizada. ");
        }

        return empresas.fieldByName("CEMPRESA").asString();
    }

    public String fazerLogin(VariavelSessao vs) throws ExcecaoTecnicon
    {
        Funcoes.validaVSNNNome(vs, new String[]
        {
            "EMAIL", "Email de login"
        }, new String[]
        {
            "SENHA", "Senha"
        });

        boolean status = false;
        int cclifor;
        JSONObject jsDados = new JSONObject();

        TClientDataSet cdsLogin = TClientDataSet.create(vs, "GSACESSO");
        cdsLogin.createDataSet();
        cdsLogin.condicao("WHERE EMAIL = '" + vs.getParameter("EMAIL").trim() + "' AND SENHA = '" + vs.getParameter("SENHA").trim() + "'");
        cdsLogin.open();
        cdsLogin.first();

        if (!cdsLogin.isEmpty())
        {
            cclifor = cdsLogin.fieldByName("CCLIFOR").asInteger();
            cdsLogin.close();

            TClientDataSet cdsDados = TClientDataSet.create(vs, "GSCLIFOR");
            cdsDados.createDataSet();
            cdsDados.condicao(new StringBuilder("WHERE CCLIFOR = '").append(cclifor).append("'").toString());
            cdsDados.open();

            if (cdsDados.fieldByName("ATIVO").asString().trim().equals("N"))
            {
                jsDados.put("STATUS", status);
                jsDados.put("MSG", "Esse usuário foi desabilitado! Contate um administrador para desbloquea-lo...");
            } else
            {
                jsDados.put("ATIVO", cdsDados.fieldByName("ATIVO"));
                jsDados.put("NOME", cdsDados.fieldByName("NOME"));
                jsDados.put("CCLIFOR", cdsDados.fieldByName("CCLIFOR"));

                status = true;
                jsDados.put("STATUS", status);
            }
            return jsDados.toString();
        } else
        {
            jsDados.put("STATUS", status);
            jsDados.put("MSG", "Usuário ou senha inválidos! Tente novamente!");
            return jsDados.toString();
        }
    }

    public String fazerCadastro(VariavelSessao vs) throws ExcecaoTecnicon
    {
        Funcoes.validaVSNNNome(vs, new String[]
        {
            "NOME", "Nome de Usuário"
        }, new String[]
        {
            "EMAIL", "Email de Login"
        }, new String[]
        {
            "SENHA", "Senha"
        }, new String[]
        {
            "CONFSENHA", "Confirmação da Senha"
        });

        if (vs.getParameter("SENHA").equals(vs.getParameter("CONFSENHA")))
        {
            int cclifor;
            JSONObject objeto = new JSONObject();

            TClientDataSet cdsCadastro = TClientDataSet.create("GSACESSO");
            cdsCadastro.createDataSet();
            cdsCadastro.condicao("WHERE EMAIL = '" + vs.getParameter("EMAIL") + "' ");
            cdsCadastro.open();

            if (!cdsCadastro.isEmpty())
            {
                throw new ExcecaoMsg(vs, "O email já está cadastrado em uma conta!");
            } else
            {
                cdsCadastro.close();
                TClientDataSet cdsCadastro2 = TClientDataSet.create(vs, "GSCLIFOR");
                cdsCadastro2.createDataSet();
                cdsCadastro2.insert();

                cdsCadastro2.fieldByName("NOME").asString(vs.getParameter("NOME"));
                cdsCadastro2.fieldByName("FANTASIA").asString(vs.getParameter("NOME"));
                cdsCadastro2.fieldByName("DTCADASTRO").asDate(new Date());
                cdsCadastro2.fieldByName("DTALT").asDate(new Date());
                cdsCadastro2.fieldByName("TIPO").asString("C");
                cdsCadastro2.fieldByName("ATIVO").asString("S");
                cdsCadastro2.fieldByName("CFILIAL").asInteger(1);
                cdsCadastro2.fieldByName("MALA").asString("W");

                cdsCadastro2.post();

                cclifor = cdsCadastro2.fieldByName("CCLIFOR").asInteger();

                cdsCadastro = TClientDataSet.create(vs, "GSACESSO");
                cdsCadastro.createDataSet();
                cdsCadastro.insert();

                cdsCadastro.fieldByName("CCLIFOR").asInteger(cclifor);
                cdsCadastro.fieldByName("EMAIL").asString(vs.getParameter("EMAIL"));
                cdsCadastro.fieldByName("SENHA").asString(vs.getParameter("SENHA"));
                cdsCadastro.fieldByName("CONFSENHA").asString(vs.getParameter("CONFSENHA"));
                cdsCadastro.post();

                objeto.put("NOME", vs.getParameter("NOME"));
                objeto.put("COD", cclifor);
                objeto.put("EMAIL", vs.getParameter("EMAIL"));
                objeto.put("STATUS", true);

                return objeto.toString();
            }
        } else
        {
            throw new ExcecaoMsg(vs, "As senhas devem ser iguais, por favor digite novamente!");
        }
    }

    public String verificaLogado(VariavelSessao vs) throws ExcecaoTecnicon
    {
        if (!vs.getParameter("COD").isEmpty())
        {
            boolean status = false;
            int cclifor = Funcoes.strToInt(vs.getParameter("COD"));
            JSONObject jsDados = new JSONObject();

            TClientDataSet cdsDados = TClientDataSet.create("CLIFOREND");
            cdsDados.createDataSet();
            cdsDados.condicao("WHERE CLIFOREND.EMAIL = '" + vs.getParameter("EMAIL") + "' ");
            cdsDados.open();

            if (cdsDados.fieldByName("ATIVO").asString().equals("S"))
            {
                if (!cdsDados.isEmpty())
                {
                    cdsDados.close();

                    cdsDados = TClientDataSet.create(vs, "CLIFOR");
                    cdsDados.createDataSet();
                    cdsDados.condicao(new StringBuilder("WHERE CLIFOR.CCLIFOR = ").append(cclifor).toString());
                    cdsDados.open();

                    jsDados.put("ATIVO", cdsDados.fieldByName("ATIVO"));
                    jsDados.put("NOME", cdsDados.fieldByName("NOME"));
                    jsDados.put("CCLIFOR", cdsDados.fieldByName("CCLIFOR"));

                } else
                {
                    jsDados.put("STATUS", status);
                }
                return jsDados.toString();
            }
        } else
        {
            return "";
        }
        return "";
    }

    public String registraCliente(VariavelSessao vs) throws ExcecaoTecnicon
    {
        TClientDataSet cidade = TClientDataSet.create(vs, "CIDADE");
        cidade.createDataSet();
        TSQLDataSetEmp buscaCidade = TSQLDataSetEmp.create(vs);
        buscaCidade.commandText("SELECT CIDADE.CCIDADE"
                + " FROM CIDADE"
                + " WHERE LOWER(CIDADE.CIDADE) = LOWER('" + vs.getParameter("cidadeCad") + "')");
        buscaCidade.open();

        if (buscaCidade.isEmpty())
        {
            buscaCidade.close();
            buscaCidade.commandText("SELECT CIDADE.CCIDADE"
                    + " FROM CIDADE"
                    + " WHERE CIDADE.CIDADE LIKE '%" + vs.getParameter("cidadeCad") + "%'");
            buscaCidade.open();
            if (buscaCidade.isEmpty())
            {
                cidade.insert();
                cidade.fieldByName("CIDADE").asString(vs.getParameter("cidadeCad"));
                cidade.fieldByName("UF").asString(vs.getParameter("ufCad"));
                buscaCidade.close();
                buscaCidade.commandText("SELECT FIRST 1 REGIAO.CREGIAO FROM REGIAO");
                buscaCidade.open();
                if (buscaCidade.recordCount() > 0)
                {
                    cidade.fieldByName("CREGIAO").asString(buscaCidade.fieldByName("CREGIAO").asString());
                }
                buscaCidade.close();
                buscaCidade.commandText("SELECT FIRST 1 PAIS.CPAIS FROM PAIS");
                buscaCidade.open();
                if (buscaCidade.recordCount() > 0)
                {
                    cidade.fieldByName("CPAIS").asString(buscaCidade.fieldByName("CPAIS").asString());
                }

                cidade.post();
            } else
            {
                cidade.close();
                cidade.condicao("WHERE CIDADE.CCIDADE = " + buscaCidade.fieldByName("CCIDADE").asString());
                cidade.open();
            }
        } else
        {
            cidade.close();
            cidade.condicao("WHERE CIDADE.CCIDADE = " + buscaCidade.fieldByName("CCIDADE").asString());
            cidade.open();
        }

        TClientDataSet clifor = TClientDataSet.create(vs, "CLIFOR");
        clifor.createDataSet();
        TClientDataSet cliExtra = TClientDataSet.create(vs, "CLIEXTRA");
        cliExtra.createDataSet();
        TClientDataSet contatoCli = TClientDataSet.create(vs, "CONTATOCLI");
        contatoCli.createDataSet();
        TClientDataSet cliforEnd = TClientDataSet.create(vs, "CLIFOREND");
        cliforEnd.createDataSet();
        cliforEnd.condicao("WHERE CLIFOREND.CGC = '" + vs.getParameter("cgcCad") + "'");
        cliforEnd.open();

        if (!cliforEnd.isEmpty())
        {
            throw new ExcecaoTecnicon(vs, "CNPJ/CPF já cadastrado!");
        }

        clifor.insert();
        clifor.fieldByName("CFILIAL").asString("1");
        clifor.fieldByName("NOME").asString(vs.getParameter("nomeCad"));
        clifor.fieldByName("FANTASIA").asString(vs.getParameter("nomeCad"));
        clifor.fieldByName("TIPO").asString("C");
        clifor.fieldByName("MALA").asString("W");
        clifor.fieldByName("ATIVO").asString("S");
        clifor.fieldByName("DTCADASTRO").asDate(new Date());
        clifor.post();

        cliforEnd.insert();
        cliforEnd.fieldByName("CCLIFOR").asString(clifor.fieldByName("CCLIFOR").asString());
        cliforEnd.fieldByName("CGC").asString(vs.getParameter("cgcCad"));
        cliforEnd.fieldByName("FILIALCF").asString("1");
        cliforEnd.fieldByName("NOMEFILIAL").asString(vs.getParameter("nomeCad"));
        cliforEnd.fieldByName("FANTASIA").asString(vs.getParameter("nomeCad"));
        cliforEnd.fieldByName("CEP").asString(vs.getParameter("cepCad"));
        cliforEnd.fieldByName("ENDERECO").asString(vs.getParameter("enderecoCad"));
        cliforEnd.fieldByName("NUMERO").asString(vs.getParameter("numeroCad"));
        cliforEnd.fieldByName("COMPLEMENTO").asString(vs.getParameter("complementoCad"));
        cliforEnd.fieldByName("BAIRRO").asString(vs.getParameter("bairroCad"));
        cliforEnd.fieldByName("CCIDADE").asString(cidade.fieldByName("CCIDADE").asString());
        cliforEnd.fieldByName("CEP1").asString(vs.getParameter("cepCad"));
        cliforEnd.fieldByName("END1").asString(vs.getParameter("enderecoCad"));
        cliforEnd.fieldByName("NUM1").asString(vs.getParameter("numeroCad"));
        cliforEnd.fieldByName("COMPLEMENTO1").asString(vs.getParameter("complementoCad"));
        cliforEnd.fieldByName("BAIRRO1").asString(vs.getParameter("bairroCad"));
        cliforEnd.fieldByName("CCIDADE1").asString(cidade.fieldByName("CCIDADE").asString());
        cliforEnd.fieldByName("FONE").asString(vs.getParameter("telefoneCad"));
        cliforEnd.fieldByName("EMAIL").asString(vs.getParameter("emailCad"));
        cliforEnd.fieldByName("DTCADASTRO").asDate(new Date());
        cliforEnd.fieldByName("ATIVO").asString("S");
        cliforEnd.fieldByName("CTIPOCLIFOR").asString("24");
        cliforEnd.post();

        contatoCli.insert();
        contatoCli.fieldByName("CCLIFOR").asInteger(cliforEnd.fieldByName("CCLIFOR").asInteger());
        contatoCli.fieldByName("FILIALCF").asInteger(cliforEnd.fieldByName("FILIALCF").asInteger());
        contatoCli.fieldByName("CONTATO").asString(cliforEnd.fieldByName("NOMEFILIAL").asString());
        contatoCli.fieldByName("VINCOMPRA").asString("C");
        contatoCli.fieldByName("EMAIL").asString(vs.getParameter("emailCad"));
        contatoCli.post();

        cliExtra.insert();
        cliExtra.fieldByName("CCLIFOR").asInteger(clifor.fieldByName("CCLIFOR").asInteger());
        cliExtra.fieldByName("DATANASC").asDate(Funcoes.strToDate(vs, vs.getParameter("nascimentoCad")));
        cliExtra.post();

        TClientDataSet cliForEndUser = TClientDataSet.create(vs, "CLIFORENDUSER");
        cliForEndUser.createDataSet();
        cliForEndUser.insert();
        cliForEndUser.fieldByName("CUSUARIO").asString("17");
        cliForEndUser.fieldByName("ATIVO").asString("S");
        cliForEndUser.fieldByName("TIPO").asString("1");//Portal do Cliente
        cliForEndUser.fieldByName("EMAIL").asString(vs.getParameter("emailCad"));
        cliForEndUser.post();
        TClassLoader.execMethod("TecniconSecurity/CliforEndUser", "afterUpdateCFEU", vs, cliForEndUser);

        TClientDataSet cliForEndUserEmp = TClientDataSet.create(vs, "CLIFORENDUSEREMP");
        cliForEndUserEmp.createDataSet();
        cliForEndUserEmp.insert();
        cliForEndUserEmp.fieldByName("SCLIFORENDUSER").asInteger(cliForEndUser.fieldByName("SCLIFORENDUSER").asInteger());
        cliForEndUserEmp.fieldByName("CEMPRESA").asString("17");
        cliForEndUserEmp.fieldByName("CCLIFOR").asInteger(cliforEnd.fieldByName("CCLIFOR").asInteger());
        cliForEndUserEmp.fieldByName("FILIALCF").asInteger(cliforEnd.fieldByName("FILIALCF").asInteger());
        cliForEndUserEmp.post();
        TClassLoader.execMethod("TecniconSecurity/CliforEndUserEmp", "afterInsertCFEU", vs, cliForEndUserEmp);

        vs.addParametros("CUSUARIO", cliForEndUser.fieldByName("CUSUARIO").asString());
        TClassLoader.execMethod("TecniconSecurity/CliforEndUser", "checkUsuario", vs);

        vs.addParametros("novasenha", vs.getParameter("senhaCad"));
        vs.addParametros("scliforenduser", cliForEndUser.fieldByName("SCLIFORENDUSER").asString());
        vs.addParametros("master", "true");
        TClassLoader.execMethod("TecniconSecurity/SenhaUsuario", "trocarSenha", vs);

        vs.setRetornoOK("OK");
        return "" + cliforEnd.fieldByName("CCLIFOR").asInteger();
    }
}
