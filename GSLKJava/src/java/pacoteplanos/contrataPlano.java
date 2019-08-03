package pacoteplanos;

import br.com.tecnicon.enviaemail.TEnviarEmail;
import br.com.tecnicon.server.context.TClassLoader;
import br.com.tecnicon.server.context.TecniconLookup;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.dataset.TSQLDataSetEmp;
import br.com.tecnicon.server.execoes.ExcecaoMsg;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.interfaces.ParametrosForm;
import br.com.tecnicon.server.model.EmailConfig;
import br.com.tecnicon.server.sessao.VariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.ejb.Stateless;
import org.json.JSONObject;

/**
 *
 * @author Lucas Kretschmer
 */
@Stateless
public class contrataPlano {

    public String contrataPlano(VariavelSessao vs) throws ExcecaoTecnicon {
        Funcoes.validaVSNNNome(vs, new String[]{
            "CUSER", "Código do Cliente"
        }, new String[]{
            "CPLANO", "Código do Plano"
        }, new String[]{
            "CPAGAMENTO", "Código do Tipo de Pagamento"
        }, new String[]{
            "DATA", "Data Atual"
        });

        JSONObject jData = new JSONObject();
        double valorPlano;

        TClientDataSet cdsCliforend = TClientDataSet.create(vs, "CLIFOREND");
        cdsCliforend.createDataSet();
        cdsCliforend.condicao("WHERE CLIFOREND.CCLIFOR = " + vs.getParameter("CUSER"));
        cdsCliforend.open();

        if (!cdsCliforend.isEmpty()) {
            jData.put("STATUS", true);

            ParametrosForm pf = (ParametrosForm) TecniconLookup.lookup("TecniconParametrosForm", "ParametrosFormImpl");
            double joia = Funcoes.strToDouble(pf.retornaRegraNegocio(vs, vs.getValor("filial"), 2553));

            TClientDataSet cdsPlano = TClientDataSet.create(vs, "GSPLANO");
            cdsPlano.createDataSet();
            cdsPlano.condicao("WHERE GSPLANO.CPLANO = " + vs.getParameter("CPLANO"));
            cdsPlano.open();

            TClientDataSet cdsPagamento = TClientDataSet.create(vs, "GSPAGAMENTO");
            cdsPagamento.createDataSet();
            cdsPagamento.condicao("WHERE GSPAGAMENTO.CPAGAMENTO = " + Funcoes.strToInt(vs.getParameter("CPAGAMENTO")));
            cdsPagamento.open();

            valorPlano = Funcoes.divideDouble(cdsPlano.fieldByName("VALORMES").asDouble(), 30d, 2, "A");
            valorPlano = Funcoes.multiplDouble(valorPlano, cdsPagamento.fieldByName("QTDEDIAS").asDouble(), 2, "A");
            valorPlano = Funcoes.somaDouble(valorPlano, joia, 10, "A");

            TClientDataSet cdsContrato = TClientDataSet.create(vs, "GSCONTRATO1");
            cdsContrato.createDataSet();
            cdsContrato.insert();
            cdsContrato.fieldByName("CPLANO").asInteger(Funcoes.strToInt(vs.getParameter("CPLANO")));
            cdsContrato.fieldByName("CCLIFOR").asInteger(cdsCliforend.fieldByName("CCLIFOR").asInteger());
            cdsContrato.fieldByName("CPAGAMENTO").asInteger(Funcoes.strToInt(vs.getParameter("CPAGAMENTO")));
            cdsContrato.fieldByName("VALOR").asDouble(valorPlano);
            cdsContrato.fieldByName("DATA").asDate(Funcoes.strToDate(vs, vs.getParameter("DATA")));
            cdsContrato.post();

            Random radom = new Random();

            enviarBoleto(vs, cdsCliforend.fieldByName("CCLIFOR").asInteger(), valorPlano, "GS-" + radom.nextInt(9999), "Pagavel até a data do vencimento!", Funcoes.incMonth(Funcoes.strToDate(vs, vs.getParameter("DATA")), 1));

            return jData.put("MSG", "Sua assinatura foi concluída com sucesso!\r\nConfira seu email com as informações do plano contratado...").toString();
        } else {
            jData.put("STATUS", false);
            jData.put("MSG", "Existem algumas informações inconsistentes em seu cadastro, \r\nverifique as configurações e tente novamente!");
            return jData.toString();
        }
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

    private void enviarBoleto(VariavelSessao vs, int pessoa, double valor, String duplicata, String obs, Date dtVenc) throws ExcecaoTecnicon {
        if (!Funcoes.varIsNull(pessoa)) {
            TClientDataSet receber = TClientDataSet.create(vs, "RECEBER");
            receber.createDataSet();
            receber.insert();
            receber.fieldByName("DATA").asDate(new Date());
            receber.fieldByName("CFILIAL").asInteger(1);
            receber.fieldByName("CCLIFOR").asInteger(pessoa);
            receber.fieldByName("FILIALCF").asInteger(1);
            receber.fieldByName("CCARTEIRA").asInteger(1);
            receber.fieldByName("DUPLICATA").asString(duplicata);
            receber.fieldByName("PARCELA").asString("1");
            receber.fieldByName("VCTO").asDate(dtVenc);
            receber.fieldByName("VCTOP").asDate(dtVenc);
            receber.fieldByName("VALOR").asDouble(valor);
            receber.fieldByName("OBS").asString(obs);
            receber.post();

            vs.addParametros("filial", "1");
            vs.addParametros("cusuario", "27");
            vs.addParametros("empresa", "17");
            vs.addParametros("usuario", "CFJL.LUCAS");
            vs.addParametros("CCARTEIRA", "1");
            vs.addParametros("cbMsgDescVcto", "false");
            vs.addParametros("CUSTOBLOQUETO", receber.fieldByName("VALOR").asString());
            vs.addParametros("SRECEBER", receber.fieldByName("SRECEBER").asString());

            try {
                TClassLoader.execMethod("BloquetoImprime/BloquetoImprime", "enviarSelecionados", vs);

            } catch (ExcecaoTecnicon ex) {
                throw new ExcecaoTecnicon(vs, ex.getMessage());
            }

        }
    }

    public String cliente_grava2(VariavelSessao vs, String CLOCALLCTO,
            int CFILIAL, String CNPJ, String IE, String NOME, String ENDERECO,
            int NUMERO, String BAIRRO,
            String CEP, String FONE, String CELULAR, String EMAIL,
            int CCIDADE, int CTIPOCLIFOR,
            String Tipo, String CCLASSIFICACAO, String FILIALATENDE,
            String PRINCIPAL, String CCARTEIRA, String CPRAZO, String DESCONTOVENCIMENTO,
            String OBSERVACOES, String CONTATO, String FANTASIA,
            String PLACA, String UFPLACA) throws ExcecaoTecnicon, NoSuchAlgorithmException {
        try {
            JSONObject objeto = new JSONObject();
            TClientDataSet cliForEndTbPreco = TClientDataSet.create(vs, "CLIFORENDTBPRECO");
            cliForEndTbPreco.createDataSet();
            String cTbPreco = "";

            TClientDataSet clifor = TClientDataSet.create(vs, "CLIFOR");
            clifor.createDataSet();
            clifor.insert();
            clifor.fieldByName("CFILIAL").asInteger(CFILIAL);
            clifor.fieldByName("NOME").asString(NOME);
            clifor.fieldByName("FANTASIA").asString(FANTASIA);
            if (!"".equals(Tipo)) {
                clifor.fieldByName("TIPO").asString(Tipo);
            } else {
                clifor.fieldByName("TIPO").asString("C");
            }
            clifor.fieldByName("CREDITO").asString("N");
            clifor.fieldByName("OSIMPLES").asString("N");
            clifor.fieldByName("SIMPLES").asString("N");
            clifor.fieldByName("ABCMAR").asString("N");
            clifor.fieldByName("ABCVENDA").asString("N");
            clifor.fieldByName("MALA").asString("W");
            clifor.fieldByName("TPPIS").asString("N");
            clifor.fieldByName("DTCADASTRO").asString(Funcoes.formatarData(new Date(), "dd.MM.yyyy"));
            clifor.fieldByName("ATIVO").asString("S");
            if (!CCARTEIRA.equals("") && !CCARTEIRA.equals("0")) {
                clifor.fieldByName("CCARTEIRA").asString(CCARTEIRA);
            }
            if (!OBSERVACOES.equals("")) {
                clifor.fieldByName("COMENT").asString(OBSERVACOES);
            }
            if (!DESCONTOVENCIMENTO.equals("") && !DESCONTOVENCIMENTO.equals("0")) {
                clifor.fieldByName("DESCVCTO").asDouble(Funcoes.strToDouble(DESCONTOVENCIMENTO));
            }
            if (!CPRAZO.equals("") && !CPRAZO.equals("0")) {
                clifor.fieldByName("CPRAZO").asString(CPRAZO);
            }
            clifor.post();
            TClientDataSet cliforend = TClientDataSet.create(vs, "CLIFOREND");
            cliforend.createDataSet();
            cliforend.insert();
            cliforend.fieldByName("CCLIFOR").asInteger(clifor.fieldByName("CCLIFOR").asInteger());
            cliforend.fieldByName("FILIALCF").asString("1");
            cliforend.fieldByName("ATIVO").asString("S");
            cliforend.fieldByName("DESTACAICMS").asString("N");
            cliforend.fieldByName("ICMSST").asString("N");
            cliforend.fieldByName("IPISUSPENSO").asString("N");
            cliforend.fieldByName("RETEMPIS").asString("N");
            cliforend.fieldByName("NOMEFILIAL").asString(NOME);
            cliforend.fieldByName("FANTASIA").asString(FANTASIA);
            cliforend.fieldByName("ENDERECO").asString(ENDERECO);
            if (NUMERO > 0) {
                cliforend.fieldByName("NUMERO").asInteger(NUMERO);
            }
            cliforend.fieldByName("BAIRRO").asString(BAIRRO);
            cliforend.fieldByName("COMPLEMENTO").asString("");
            cliforend.fieldByName("CCIDADE").asInteger(CCIDADE);
            cliforend.fieldByName("CEP").asString(CEP);
            cliforend.fieldByName("CGC").asString(CNPJ);
            cliforend.fieldByName("IE").asString(IE);
            cliforend.fieldByName("CCIDADE1").asInteger(CCIDADE);
            cliforend.fieldByName("END1").asString(ENDERECO);
            if (!CONTATO.equals("")) {
                cliforend.fieldByName("CONTATO").asString(CONTATO);
            }

            if (!cTbPreco.equals("")) {
                cliforend.fieldByName("CTBPRECO").asString(cTbPreco);
            }
            if (NUMERO > 0) {
                cliforend.fieldByName("NUM1").asInteger(NUMERO);
            }
            cliforend.fieldByName("BAIRRO1").asString(BAIRRO);
            cliforend.fieldByName("COMPLEMENTO1").asString("");
            cliforend.fieldByName("CEP1").asString(CEP);
            cliforend.fieldByName("FONE").asString(FONE);
            cliforend.fieldByName("CELULAR").asString(CELULAR);
            if (!"".equals(EMAIL)) {
                cliforend.fieldByName("EMAIL").asString(EMAIL);
            }
            if (!OBSERVACOES.equals("")) {
                cliforend.fieldByName("COMENT").asString(OBSERVACOES);
            }
            if (!"".equals(FILIALATENDE)) {
                cliforend.fieldByName("CFILIALATENDE").asString(FILIALATENDE);
            }
            if (CTIPOCLIFOR != 0) {
                cliforend.fieldByName("CTIPOCLIFOR").asInteger(CTIPOCLIFOR);
            }
            cliforend.fieldByName("DTCADASTRO").asString(Funcoes.formatarData(new Date(), "dd.MM.yyyy"));
            cliforend.fieldByName("PLACA").asString(PLACA);
            cliforend.fieldByName("UFPLACA").asString(UFPLACA);
            cliforend.post();

            TClientDataSet contatoCli = TClientDataSet.create(vs, "CONTATOCLI");
            contatoCli.createDataSet();
            contatoCli.insert();
            contatoCli.fieldByName("CCLIFOR").asInteger(cliforend.fieldByName("CCLIFOR").asInteger());
            contatoCli.fieldByName("FILIALCF").asInteger(1);
            contatoCli.fieldByName("CONTATO").asString(NOME);
            contatoCli.fieldByName("VINCOMPRA").asString("F");
            if (!"".equals(EMAIL)) {
                contatoCli.fieldByName("EMAIL").asString(EMAIL);
            }
            contatoCli.fieldByName("ATIVO").asString("S");
            contatoCli.post();

            TClientDataSet senha = TClientDataSet.create(vs, "GSACESSO");
            senha.createDataSet();
            senha.insert();
            senha.fieldByName("CCLIFOR").asInteger(cliforend.fieldByName("CCLIFOR").asInteger());
            senha.fieldByName("EMAIL").asString(EMAIL);
            senha.fieldByName("SENHA").asString(vs.getParameter("SENHA"));
            senha.fieldByName("CONFSENHA").asString(vs.getParameter("CONFSENHA"));
            senha.post();

            if (!cTbPreco.equals("")) {
                cliForEndTbPreco.insert();
                cliForEndTbPreco.fieldByName("CCLIFOR").asString(cliforend.fieldByName("CCLIFOR").asString());
                cliForEndTbPreco.fieldByName("FILIALCF").asString(cliforend.fieldByName("FILIALCF").asString());
                cliForEndTbPreco.fieldByName("CTBPRECO").asString(cTbPreco);
                cliForEndTbPreco.post();
            }

            if (!"".equals(CCLASSIFICACAO)) {
                TClientDataSet cliforclas = TClientDataSet.create(vs, "CLIFORCLAS");
                cliforclas.createDataSet();
                cliforclas.insert();
                cliforclas.fieldByName("CCLIFOR").asInteger(clifor.fieldByName("CCLIFOR").asInteger());
                cliforclas.fieldByName("CCLASSIFICACAO").asString(CCLASSIFICACAO);
                cliforclas.fieldByName("PRINCIPAL").asString(PRINCIPAL);
                cliforclas.post();
            }

            objeto.put("NOME", vs.getParameter("NOME"));
            objeto.put("COD", clifor.fieldByName("CCLIFOR").asInteger());
            objeto.put("EMAIL", EMAIL);
            objeto.put("STATUS", true);

            return objeto.toString();
        } catch (ExcecaoTecnicon ex) {
            throw new ExcecaoMsg(vs, "As senhas devem ser iguais, por favor digite novamente!");
        }
    }

    public String user(VariavelSessao vs) throws ExcecaoTecnicon, NoSuchAlgorithmException {
        TSQLDataSetEmp sql = TSQLDataSetEmp.create(vs);
        sql.commandText("SELECT"
                + "     CLIFOR.CCLIFOR, "
                + "     CLIFOREND.FILIALCF, "
                + "     CLIFOR.NOME,"
                + "     COALESCE(CLIFOREND.ATIVO,'N') AS ATIVO "
                + " FROM CLIFOREND"
                + " INNER JOIN CLIFOR ON (CLIFOR.CCLIFOR = CLIFOREND.CCLIFOR) "
                + " WHERE CLIFOREND.EMAIL = '" + vs.getParameter("EMAIL") + "'");
        sql.open();

        if (!sql.isEmpty()) {
            if (sql.fieldByName("ATIVO").asString().equals("S")) {
                return "true:" + sql.fieldByName("CCLIFOR").asString() + "-" + sql.fieldByName("FILIALCF").asString() + ":" + sql.fieldByName("NOME").asString() + ":existe";
            } else if (sql.fieldByName("ATIVO").asString().equals("N")) {
                return "false:Usuário inativo, favor contactar um adiministrador!";
            } else {
                return "false ";
            }
        } else {
            vs.addParametros("ENDERECO", "");
            vs.addParametros("BAIRRO", "");
            vs.addParametros("SENHA", vs.getParameter("SENHA"));
            vs.addParametros("CONFSENHA", vs.getParameter("CONFSENHA"));
            return gravarClienteRapido2(vs);
        }
    }

    public String gravarClienteRapido2(VariavelSessao vs) throws ExcecaoTecnicon, NoSuchAlgorithmException {
        if (vs.getParameter("SENHA").trim().equals(vs.getParameter("CONFSENHA").trim()) && vs.getParameter("SENHA").length() > 0) {
            String nome = vs.getParameter("NOME");
            if (nome.equals("")) {
                return "Nome deve estar preenchido!";
            }
            String endereco = vs.getParameter("ENDERECO");

            int numero = 0;
            if (vs.getParameter("NUMERO") != null && !vs.getParameter("NUMERO").equals("")) {
                numero = Funcoes.strToInt(vs.getParameter("NUMERO"));
            }
            String bairro = vs.getParameter("BAIRRO");
            int ccidade = 17;
            String cep = "";
            String fone = "";
            String celular = "";
            String cgc = "";
            String ie = "";
            String email = vs.getParameter("EMAIL");
            if (email.equals("")) {
                return "E-mail deve estar preenchido!";
            } else {
                TClientDataSet validaEmail = TClientDataSet.create(vs, "CLIFOREND");
                validaEmail.createDataSet();
                validaEmail.condicao(" WHERE CLIFOREND.EMAIL= '" + email + "'");
                validaEmail.open();
                if (!validaEmail.isEmpty()) {
                    return "Esse E-mail já existe, favor informar outro E-mail!";
                }
            }
            int ctipoclifor = 0;
            String contato = (vs.getParameter("CONTATO") == null ? "" : vs.getParameter("CONTATO"));
            String cclassificacao = "";
            String tipo = "";
            String cCarteira = (vs.getParameter("CCARTEIRA") == null ? "" : vs.getParameter("CCARTEIRA"));
            String cPrazo = (vs.getParameter("CPRAZO") == null ? "" : vs.getParameter("CPRAZO"));
            String descVcto = (vs.getParameter("DESCVCTO") == null ? "0" : vs.getParameter("DESCVCTO"));
            String obs = (vs.getParameter("COMENT") == null ? "" : vs.getParameter("COMENT"));
            String fantasia = nome;
            String placa = (vs.getParameter("PLACA") == null ? "" : vs.getParameter("PLACA"));
            String ufPlaca = (vs.getParameter("UFPLACA") == null ? "" : vs.getParameter("UFPLACA"));
            return cliente_grava2(vs, "", 1, cgc, ie, nome, endereco,
                    numero, bairro, cep, fone, celular, email,
                    ccidade, ctipoclifor, tipo, cclassificacao, "", "S",
                    cCarteira, cPrazo, descVcto, obs, contato, fantasia, placa, ufPlaca);

        } else {
            return "Senhas não conferem!";
        }
    }

}
