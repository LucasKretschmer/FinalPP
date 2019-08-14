package contrato;

import br.com.tecnicon.componente.CriaComponentesHtml;
import br.com.tecnicon.enviaemail.TEnviarEmail;
import br.com.tecnicon.server.context.TClassLoader;
import br.com.tecnicon.server.context.TecniconLookup;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.dataset.TSQLDataSetEmp;
import br.com.tecnicon.server.ecf.TecniRetorna;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.interfaces.ParametrosForm;
import br.com.tecnicon.server.interfaces.RelatorioEsp;
import br.com.tecnicon.server.model.EmailConfig;
import br.com.tecnicon.server.nfs.GeraLivros;
import br.com.tecnicon.server.nfs.NfsImpostoItem;
import br.com.tecnicon.server.sessao.VariavelSessao;
import br.com.tecnicon.server.tecniproc.UDmCes;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.Stateless;

/**
 *
 * @author Lucas Kretschmer
 */
@Stateless
public class realizaContrato {

    public String cadastroContrato(VariavelSessao vs) throws ExcecaoTecnicon {
        try {
            TClientDataSet cdsContrato = TClientDataSet.create(vs, "CONTRATOCLIENTE");
            cdsContrato.createDataSet();
            cdsContrato.condicao(" WHERE CONTRATOCLIENTE.NUMCONTRATO LIKE '%GS%' "
                    + " ORDER BY CONTRATOCLIENTE.NUMCONTRATO DESC");
            cdsContrato.open();

            TClientDataSet cdsClifor = TClientDataSet.create(vs, "CLIFOR");
            cdsClifor.createDataSet();
            cdsClifor.condicao(" WHERE CLIFOR.CCLIFOR = " + vs.getParameter("CCLIFOR"));
            cdsClifor.open();

            TClientDataSet cdsCliforend = TClientDataSet.create(vs, "CLIFOREND");
            cdsCliforend.createDataSet();
            cdsCliforend.condicao(" WHERE CLIFOREND.CCLIFOR = " + Funcoes.strToInt(vs.getParameter("CCLIFOR")));
            cdsCliforend.open();

            TClientDataSet cdsPlano = TClientDataSet.create(vs, "GSPLANO");
            cdsPlano.createDataSet();
            cdsPlano.condicao(" WHERE GSPLANO.CPLANO = " + vs.getParameter("CPLANO"));
            cdsPlano.open();

            TClientDataSet cdsObj = TClientDataSet.create(vs, "GSPLANOBJ");
            cdsObj.createDataSet();
            cdsObj.condicao(" WHERE GSPLANOBJ.CPLANO = " + vs.getParameter("CPLANO"));
            cdsObj.open();

            int parcelas = cdsObj.fieldByName("QTDE").asInteger();

            switch (parcelas) {
                case 3:
                    parcelas = 5;
                    break;
                case 12:
                    parcelas = 7;
                    break;
                default:
                    parcelas = 3;
                    break;
            }

            String numContrato = cdsContrato.fieldByName("NUMCONTRATO").asString().replace("GS", "");
            cdsContrato.close();

            cdsContrato.insert();
            cdsContrato.fieldByName("CONTRATOCLIENTE").asString(cdsPlano.fieldByName("NOME").asString() + " - " + cdsClifor.fieldByName("NOME").asString());
            cdsContrato.fieldByName("CFILIAL").asInteger(1);
            cdsContrato.fieldByName("NUMCONTRATO").asString("GS" + (Funcoes.strToInt(numContrato) + 1));
            cdsContrato.fieldByName("DTCONTRATO").asDate(new Date());
            cdsContrato.fieldByName("STATUS").asString("A");
            cdsContrato.fieldByName("DTINICIAL").asDate(new Date());
            cdsContrato.fieldByName("DTFINAL").asDate(Funcoes.incMonth(new Date(), cdsObj.fieldByName("QTDE").asInteger()));
            cdsContrato.fieldByName("CCONTRATONATUREZA").asInteger(1);
            cdsContrato.fieldByName("CCONTRATOTIPO").asInteger(2);
            cdsContrato.fieldByName("FILIALCF").asInteger(1);
            cdsContrato.fieldByName("CCONTRATOSTATUS").asInteger(1);
            cdsContrato.fieldByName("CCLIFOR").asInteger(Funcoes.strToInt(vs.getParameter("CCLIFOR")));
            cdsContrato.fieldByName("CCUSTO").asInteger(6);
            cdsContrato.fieldByName("CCARTEIRA").asInteger(1);
            cdsContrato.fieldByName("CPRAZO").asInteger(parcelas);
            cdsContrato.fieldByName("CIOF").asInteger(530301);
            cdsContrato.post();

            TClientDataSet cdsVigendcia = TClientDataSet.create(vs, "CONTRATOVIGENCIA");
            cdsVigendcia.createDataSet();
            cdsVigendcia.insert();
            cdsVigendcia.fieldByName("CCONTRATOCLIENTE").asInteger(cdsContrato.fieldByName("CCONTRATOCLIENTE").asInteger());
            cdsVigendcia.fieldByName("CONTRATOVIGENCIA").asString("VIGENCIA - " + cdsContrato.fieldByName("CONTRATOCLIENTE").asString());
            cdsVigendcia.fieldByName("DTINICIAL").asDate(new Date());
            cdsVigendcia.fieldByName("DTFINAL").asDate(Funcoes.incMonth(new Date(), cdsObj.fieldByName("QTDE").asInteger()));
            cdsVigendcia.fieldByName("DATA").asDate(new Date());
            cdsVigendcia.fieldByName("CUSUARIO").asInteger(27);
            cdsVigendcia.fieldByName("MESREAJUSTE").asInteger(6);
            cdsVigendcia.fieldByName("PERCJUROS").asDouble(1.0);
            cdsVigendcia.fieldByName("PERCMULTA").asDouble(1.0);
            cdsVigendcia.fieldByName("OBS").asString("Vigencia do contrato realizado pela empresa com o cliente " + cdsClifor.fieldByName("NOME").asString() + ".");
            cdsVigendcia.post();

            TClientDataSet cdsObjetosContrato = TClientDataSet.create(vs, "CONTRATOOBJETO");
            cdsObjetosContrato.createDataSet();

            cdsObj.first();
            int tipoObj;
            while (!cdsObj.eof()) {
                if (cdsObj.fieldByName("NOMEOBJ").asString().contains("JOIA")) {
                    tipoObj = 2;
                } else if (cdsObj.fieldByName("NOMEOBJ").asString().contains("MENSALIDADE")) {
                    tipoObj = 1;
                } else {
                    tipoObj = 3;
                }

                cdsObjetosContrato.insert();
                cdsObjetosContrato.fieldByName("CCONTRATOVIGENCIA").asInteger(cdsVigendcia.fieldByName("CCONTRATOVIGENCIA").asInteger());
                cdsObjetosContrato.fieldByName("CCONTRATOOBJ").asInteger(tipoObj);
                cdsObjetosContrato.fieldByName("UNITARIO").asDouble(cdsObj.fieldByName("VALORUN").asDouble());
                cdsObjetosContrato.fieldByName("QTDE").asDouble(cdsObj.fieldByName("QTDE").asDouble());
                cdsObjetosContrato.post();
                cdsObj.next();
            }

            String anoMes = Funcoes.anoMesRH(vs, cdsContrato.fieldByName("DTINICIAL").asDate());
            Date dtFaturamento = Funcoes.incMonth(new Date(), 1);

            vs.addParametros("ANOMES", anoMes + "");
            vs.addParametros("filial", "1");
            vs.addParametros("DTFATURAMENTO", Funcoes.dateToStr(dtFaturamento));
            vs.addParametros("grid", cdsContrato.fieldByName("CCONTRATOCLIENTE").asString());

            vs.addParametros("cusuario", "27");
            vs.addParametros("empresa", "17");
            vs.addParametros("usuario", "CFJL.LUCAS");
            vs.addParametros("CCARTEIRA", "1");
            vs.addParametros("cbMsgDescVcto", "false");

            gerarDuplicata(vs);

            TClientDataSet receber = TClientDataSet.create(vs, "RECEBER");
            receber.createDataSet();
            receber.condicao(" WHERE (COALESCE(RECEBER.VALOR, 0) - "
                    + "        (SELECT "
                    + "            COALESCE(SUM(BXRECEBER.VALOR), 0) "
                    + "        FROM BXRECEBER WHERE BXRECEBER.SRECEBER = RECEBER.SRECEBER) "
                    + "    ) > 0 AND RECEBER.CCLIFOR = 33176 " /*+ cdsClifor.fieldByName("CCLIFOR").asInteger()*/);
            receber.open();

            new ExcecaoTecnicon(vs, "Foi = " + receber.recordCount(), new Throwable(), true);

            while (!receber.eof()) {
                vs.removeParametro("CUSTOBLOQUETO");
                vs.addParametros("CUSTOBLOQUETO", receber.fieldByName("VALOR").asString());
                vs.removeParametro("SRECEBER");
                vs.addParametros("SRECEBER", receber.fieldByName("SRECEBER").asString());
                enviarSelecionados(vs);
                receber.next();
            }
        } catch (ExcecaoTecnicon ex) {
            return ex.getMessage();
        }

        return "Contrato gerado com sucesso, verifique seu email para receber os boletos!";
    }

    public void enviarSelecionados(VariavelSessao vs) throws ExcecaoTecnicon {
        new ExcecaoTecnicon(vs, "entrou 1 = ", new Throwable(), true);
        try {

            String[] retorno = new String[1];
            retorno[0] = "";
            int CCARTEIRA = Funcoes.strToInt(vs.getParameter("CCARTEIRA"));
            String SRECEBER = vs.getParameter("SRECEBER");
            String cbMsgDescVcto = vs.getParameter("cbMsgDescVcto");
            String CUSTOBLOQUETO = vs.getParameter("CUSTOBLOQUETO");
            String remetentes = "";
            TClientDataSet carteira = null;
            TClientDataSet receber = null;
            TClientDataSet cliForEnd = null;
            TSQLDataSetEmp cdsCom1 = null;
            String XEMAILTEXTO;
            TClientDataSet filial = null;
            TSQLDataSetEmp CDSC = null;

            /*DADOS PARA MOSTRAR CASO OCORRA ERRO*/
            int cliente = 0;
            int filialCli = 0;
            String nomeCli = "";

            boolean msgTrow = false;
            Map<String, byte[]> anexos = new HashMap<>();
            try {
                CDSC = TSQLDataSetEmp.create(vs);
                receber = TClientDataSet.create(vs, "RECEBER");
                receber.createDataSet();
                receber.condicao(" WHERE RECEBER.SRECEBER = " + SRECEBER);
                receber.open();

                filial = TClientDataSet.create(vs, "FILIAL");
                filial.createDataSet();
                filial.condicao(" WHERE FILIAL.CFILIAL = " + vs.getValor("filial"));
                filial.open();

                cliForEnd = TClientDataSet.create(vs, "CLIFOREND");
                cliForEnd.createDataSet();
                cliForEnd.close();
                cliForEnd.condicao(" WHERE CLIFOREND.CCLIFOR=" + receber.fieldByName("CCLIFOR").asInteger()
                        + " AND CLIFOREND.FILIALCF=" + receber.fieldByName("FILIALCF").asInteger());
                cliForEnd.open();

                cliente = cliForEnd.fieldByName("CCLIFOR").asInteger();
                filialCli = cliForEnd.fieldByName("FILIALCF").asInteger();
                nomeCli = cliForEnd.fieldByName("NOMEFILIAL").asString();

                carteira = TClientDataSet.create(vs, "CARTEIRA");
                carteira.createDataSet();

                carteira.close();
                carteira.condicao(" WHERE CARTEIRA.CCARTEIRA=" + CCARTEIRA + " AND CARTEIRA.ATIVO = 'S'");
                carteira.open();
                if (carteira.isEmpty() == true) {
                    msgTrow = true;
                    throw new ExcecaoTecnicon(vs, "Cód. Cliente: " + cliForEnd.fieldByName("CCLIFOR").asString() + " Filial: " + cliForEnd.fieldByName("FILIALCF").asString() + " Nome: " + cliForEnd.fieldByName("NOMEFILIAL").asString() + "|TEC#|<br>E-mail não foi enviado, código carteira não localizado ou inativo!");
                }

                cdsCom1 = TSQLDataSetEmp.create(vs);

                if (("" + TClassLoader.execMethod("Bloquetos/Bloquetos", "IMPRIMEBLOQUETO", vs, CCARTEIRA, "N", Boolean.parseBoolean(cbMsgDescVcto), "S", receber, retorno, Funcoes.strToDouble((CUSTOBLOQUETO != null && !CUSTOBLOQUETO.equals("") ? CUSTOBLOQUETO : (CUSTOBLOQUETO == null || CUSTOBLOQUETO.equals("") ? "0,00" : CUSTOBLOQUETO))))).equals("RELATORIO3063")) {
                    msgTrow = true;
                    throw new ExcecaoTecnicon(vs, "MOSTRA|TEC#|Cód. Cliente: " + cliForEnd.fieldByName("CCLIFOR").asString() + " Filial: " + cliForEnd.fieldByName("FILIALCF").asString() + " Nome: " + cliForEnd.fieldByName("NOMEFILIAL").asString() + "|TEC#|<br>E-mail não foi enviado.");
                }

                receber.close();
                receber.condicao(" WHERE RECEBER.SRECEBER = " + SRECEBER);
                receber.open();

                cdsCom1.close();
                cdsCom1.commandText(
                        " SELECT CONTATOCLI.EMAIL FROM CONTATOCLI "
                        + " WHERE CONTATOCLI.CCLIFOR=" + receber.fieldByName("CCLIFOR").asInteger() + " "
                        + " AND CONTATOCLI.FILIALCF=" + receber.fieldByName("FILIALCF").asInteger());
                cdsCom1.open();

                while (!cdsCom1.eof()) {
                    remetentes += cdsCom1.fieldByName("EMAIL").asString() + ";";
                    cdsCom1.next();
                }

                CDSC.close();
                CDSC.commandText("SELECT BLOQUETO.CBLOQUETO,BLOQUETO.MENS,BLOQUETO.LOCALPGTO"
                        + " FROM BLOQUETO"
                        + " WHERE BLOQUETO.CBLOQUETO=" + carteira.fieldByName("CBLOQUETO").asString());
                CDSC.open();
                int BLOQ_CBLOQUETO = CDSC.fieldByName("CBLOQUETO").asInteger();

                CDSC.close();
                CDSC.commandText(" SELECT BLOQNOSSONUMERO.MODELOBOLETO, BLOQNOSSONUMERO.TPCONVENIO, TSUBSTR(BLOQNOSSONUMERO.CODBANCO,1,3) CODBANCO "
                        + " FROM BLOQNOSSONUMERO"
                        + " WHERE BLOQNOSSONUMERO.CBLOQUETO=" + BLOQ_CBLOQUETO
                        + " AND BLOQNOSSONUMERO.CFILIAL=" + vs.getValor("filial"));
                CDSC.open();

                XEMAILTEXTO = ("Prezado(a) " + cliForEnd.fieldByName("NOMEFILIAL").asString()) + "<br />";
                XEMAILTEXTO += ("Segue em anexo o boleto com data de vencimento " + receber.fieldByName("VCTO").asString()) + "<br />";
                XEMAILTEXTO += "Dupl: " + (receber.fieldByName("DUPLICATA").asString() + " <br /> Parc: " + receber.fieldByName("PARCELA").asString() + "<br />");
                XEMAILTEXTO += ("<br />");
                XEMAILTEXTO += ("<br />");
                XEMAILTEXTO += ("Este e-mail foi gerado pelo sistema TECNICON Business Suite na empresa " + filial.fieldByName("RAZAO_SOCIAL").asString());

                RelatorioEsp relEsp = (RelatorioEsp) TecniconLookup.lookup("TecniconRelatorioEsp", "RelatorioEsp");
                VariavelSessao vs2 = vs.clone();
                vs2.addParametros("SRECEBER", SRECEBER);
                vs2.addParametros("IMPTODAS", "FALSE");
                vs2.addParametros("FILTROS", "");
                vs2.addParametros("IMPNAO", "FALSE");
                vs2.addParametros("CFILIAL", vs.getValor("filial"));

                switch (CDSC.fieldByName("MODELOBOLETO").asString()) {
                    case "1":
                        vs2.addParametros("relatorioesp", "132");
                        break;
                    case "2":
                        vs2.addParametros("relatorioesp", "1911");
                        break;
                    case "3":
                        vs2.addParametros("relatorioesp", "1931");
                        break;
                    case "5":
                        vs2.addParametros("relatorioesp", "1921");
                        break;
                    default:
                        if ("4".equals(CDSC.fieldByName("MODELOBOLETO").asString())) {
                            vs2.addParametros("relatorioesp", "1901");
                        } else if (vs.getValor("nomeFilial").contains("RHRISS")) {
                            vs2.addParametros("relatorioesp", "1941");
                        } else if (("07".equals(CDSC.fieldByName("TPCONVENIO").asString())) || ("748".equals(CDSC.fieldByName("CODBANCO").asString()))) {
                            vs2.addParametros("relatorioesp", "1931");
                        } else if (("001".equals(CDSC.fieldByName("CODBANCO").asString()))
                                || ("027".equals(CDSC.fieldByName("CODBANCO").asString()))
                                || ("041".equals(CDSC.fieldByName("CODBANCO").asString()))
                                || ("104".equals(CDSC.fieldByName("CODBANCO").asString()))
                                || ("237".equals(CDSC.fieldByName("CODBANCO").asString()))) {
                            vs2.addParametros("relatorioesp", "132");
                        } else if ("5".equals(CDSC.fieldByName("MODELOBOLETO").asString())) {
                            vs2.addParametros("relatorioesp", "1921");
                        } else {
                            vs2.addParametros("relatorioesp", "1911");
                        }
                        break;
                }
                relEsp.gerarRelatorio(vs2);

                TClientDataSet cdsUserMail = TClientDataSet.create(vs, "USUARIOEMAIL");
                cdsUserMail.createDataSet();
                cdsUserMail.condicao("WHERE USUARIOEMAIL.CUSUARIO=" + vs.getValor("cusuario"));
                cdsUserMail.open();

                if (!"".equals(remetentes)) {

                    /* Otimização  - Ilario 19/02/15*/
                    anexos.put(receber.fieldByName("NOSSONUMERO").asString() + ".pdf", Funcoes.convertStringToByte(vs2.getRetornoOK()));

                    EmailConfig config = new EmailConfig(cdsUserMail.fieldByName("USUARIO").asString(), cdsUserMail.fieldByName("SENHA").asString(),
                            cdsUserMail.fieldByName("HOSTSMTP").asString(), cdsUserMail.fieldByName("EMAIL").asString(), cdsUserMail.fieldByName("NOME").asString(),
                            "", "", cdsUserMail.fieldByName("PORTSMTP").asInteger(),
                            cdsUserMail.fieldByName("SSL").asString(), Integer.parseInt(vs.getValor("empresa")), 1);

                    String assunto = filial.fieldByName("FANTASIA").asString() + " - boleto com vencimento em " + receber.fieldByName("VCTO").asString()
                            + " " + receber.fieldByName("DUPLICATA").asString() + "/" + receber.fieldByName("PARCELA").asString();

                    /* Otimização  - Ilario 19/02/15*/
                    new TEnviarEmail().enviarEmail(remetentes, "", "", assunto, XEMAILTEXTO, config, anexos);

                    vs.setRetornoOK("Cód. Cliente: " + cliForEnd.fieldByName("CCLIFOR").asString() + " Filial: " + cliForEnd.fieldByName("FILIALCF").asString() + " Nome: " + cliForEnd.fieldByName("NOMEFILIAL").asString() + "|TEC#|<br>E-mail enviado, para: " + remetentes + "|TEC#|" + retorno[0]);
                } else {
                    msgTrow = true;
                    throw new ExcecaoTecnicon(vs, "MOSTRA|TEC#|Cód. Cliente: " + cliForEnd.fieldByName("CCLIFOR").asString() + " Filial: " + cliForEnd.fieldByName("FILIALCF").asString() + " Nome: " + cliForEnd.fieldByName("NOMEFILIAL").asString() + "|TEC#|<br>E-mail não foi enviado, não possui destinatário.");
                    /*sol: 149398 - erro de descricao */

                }
            } catch (ExcecaoTecnicon e) {
                if (msgTrow) {
                    throw new ExcecaoTecnicon(vs, e);
                }

                if (cliente > 0) {
                    throw new ExcecaoTecnicon(vs, "Cód. Cliente: " + cliente + " Filial: " + filialCli + " Nome: " + nomeCli + "|TEC#|E-mail não foi enviado, para " + (remetentes.isEmpty() ? "\"Sem remetente\"" : remetentes) + "<br>Erro: " + e.getMessage() + ".");
                } else {
                    throw new ExcecaoTecnicon(vs, "E-mail não foi enviado, para " + (remetentes.isEmpty() ? "\"Sem remetente\"" : remetentes) + "|TEC#|Erro: " + e.getMessage() + ".");
                }
            } finally {
                if (carteira != null) {
                    carteira.close();
                }

                if (receber != null) {
                    receber.close();
                }

                if (filial != null) {
                    filial.close();
                }

                if (CDSC != null) {
                    CDSC.close();
                }

                if (cdsCom1 != null) {
                    cdsCom1.close();
                }

                if (cliForEnd != null) {
                    cliForEnd.close();
                }
            }
        } catch (Exception e) {
            throw new ExcecaoTecnicon(vs, "DEU RUIM NO EMAIL: " + e.getMessage(), e, true);
        }
    }

    public String gerarDuplicata(VariavelSessao vs) throws ExcecaoTecnicon {
        try {
            Funcoes.validaVSNN(vs, "ANOMES", "grid");

            String retorno;
            ParametrosForm pf = (ParametrosForm) TecniconLookup.lookup("TecniconParametrosForm", "ParametrosFormImpl");
            String[] regra = pf.retornaRegraNegocio(vs, vs.getValor("filial"), 245, 1905);

            vs.addParametros("contratoCliente", "S");

            retorno = criaNota(vs, Funcoes.strToInt(vs.getParameter("grid")), regra);

            return retorno;
        } catch (ExcecaoTecnicon ex) {
            throw new ExcecaoTecnicon(vs, "erroooo; " + ex.getMessage(), ex, true);
        }
    }

    public String formShow(VariavelSessao vs) throws ExcecaoTecnicon {
        return carregarGrid(vs);
    }

    public String carregarGrid(VariavelSessao vs) throws ExcecaoTecnicon {
        TClientDataSet cdsGrid = TClientDataSet.create(vs);
        cdsGrid.createDataSet();
        cdsGrid.commandText(" SELECT 'S' AS CB,"
                + "     CONTRATOCLIENTE.CCONTRATOCLIENTE,"
                + "     CONTRATOCLIENTE.CCLIFOR,"
                + "     CONTRATOCLIENTE.FILIALCF,"
                + "     CLIFOREND.NOMEFILIAL,"
                + "     CONTRATOCLIENTE.CFILIAL,"
                + "     CONTRATOCLIENTE.DTINICIAL,"
                + "     CONTRATOCLIENTE.DTFINAL, "
                + "     (SELECT SUM(COALESCE(CONTRATOOBJETO.UNITARIO, 0) * COALESCE(CONTRATOOBJETO.QTDE, 0))"
                + "      FROM CONTRATOOBJETO"
                + "      INNER JOIN CONTRATOVIGENCIA ON (CONTRATOVIGENCIA.CCONTRATOVIGENCIA = CONTRATOOBJETO.CCONTRATOVIGENCIA)"
                + "      WHERE CONTRATOVIGENCIA.CCONTRATOCLIENTE = CONTRATOCLIENTE.CCONTRATOCLIENTE) AS VALOR "
                + " FROM CONTRATOCLIENTE"
                + " INNER JOIN CLIFOREND ON (CLIFOREND.CCLIFOR = CONTRATOCLIENTE.CCLIFOR AND CLIFOREND.FILIALCF = CONTRATOCLIENTE.FILIALCF)");

        if (Funcoes.validaVSCampo(vs, "ANOMES")) {
            String ano = Funcoes.copy(vs, vs.getParameter("ANOMES"), 0, 4);
            String mes = Funcoes.copy(vs, vs.getParameter("ANOMES"), 5, 2);
            cdsGrid.condicao(" WHERE CONTRATOCLIENTE.DTFINAL >= '" + Funcoes.formatarDB(("01/" + mes + "/" + ano), "D") + "'"
                    + " AND CONTRATOCLIENTE.DTINICIAL <= '" + Funcoes.formatarDB("" + Funcoes.ultimoDia(vs, mes, ano), "D") + "'"
                    + " AND CONTRATOCLIENTE.STATUS = 'A' "
                    /* so vai listar os que nao foram faturados*/
                    + " AND NOT EXISTS ("
                    + "         SELECT NFSAIDA.CCLIFOR, NFSAIDA.FILIALCF"
                    + "  	FROM NFSAIDA "
                    + "  	WHERE NFSAIDA.CCLIFOR = CONTRATOCLIENTE.CCLIFOR AND NFSAIDA.FILIALCF = CONTRATOCLIENTE.FILIALCF"
                    + "          AND NFSAIDA.CIOF = CONTRATOCLIENTE.CIOF "
                    + "  	AND NFSAIDA.DATA BETWEEN '" + Funcoes.formatarDB(("01/" + mes + "/" + ano), "D") + "' AND '" + Funcoes.formatarDB("" + Funcoes.ultimoDia(vs, mes, ano), "D") + "'"
                    + ")");
        } else {
            cdsGrid.condicao("WHERE CONTRATOCLIENTE.STATUS = 'A'");
        }

        cdsGrid.open();
        formataClient(cdsGrid);

        CriaComponentesHtml comp = new CriaComponentesHtml();
        comp.vsInt = vs;
        return comp.criaGrid(cdsGrid, "UNICA", "stylePosition=initial;styleHeight:330px;usaCds=true", "");
    }

    public String criaNota(VariavelSessao vs, int cod, String[] regra) throws ExcecaoTecnicon {
        try {
            TClientDataSet contratoCliente = TClientDataSet.create(vs, "CONTRATOCLIENTE");
            contratoCliente.createDataSet();
            contratoCliente.condicao(" WHERE CONTRATOCLIENTE.CCONTRATOCLIENTE = " + cod);
            contratoCliente.open();
            if (contratoCliente.fieldByName("CIOF").isNull()) {
                return "erro:Não foi encontrado o CIOF configurado no contrato " + cod + ", dessa forma, não é possível gerar o Faturamento deste contrato.";
            }

            TSQLDataSetEmp contratoCliNF = TSQLDataSetEmp.create(vs);
            contratoCliNF.commandText(
                    " SELECT NFSAIDA.NFS "
                    + " FROM CONTRATOCLIENTENF "
                    + " INNER JOIN NFSAIDA ON (NFSAIDA.NFS = CONTRATOCLIENTENF.NFS) "
                    + " WHERE CONTRATOCLIENTENF.CCONTRATOCLIENTE = " + cod
                    + "   AND TYEAR(NFSAIDA.DATA) = " + String.valueOf(Funcoes.yearOf(new Date()))
                    + "   AND TMONTH(NFSAIDA.DATA) = " + String.valueOf(Funcoes.monthOf(new Date())));
            contratoCliNF.open();
            if (!contratoCliNF.isEmpty()) {
                return "Já foi gerada a NF de seq. " + contratoCliNF.fieldByName("NFS").asInteger() + " para o contrato " + cod + "!";
            }

            int CCLIFOR = contratoCliente.fieldByName("CCLIFOR").asInteger();
            int FILIALCF = contratoCliente.fieldByName("FILIALCF").asInteger();

            TClientDataSet cliforend = TClientDataSet.create(vs, "CLIFOREND");
            cliforend.createDataSet();
            cliforend.condicao("WHERE CLIFOREND.CCLIFOR = " + CCLIFOR + " AND CLIFOREND.FILIALCF = " + FILIALCF);
            cliforend.open();

            if (!cliforend.isEmpty()) {
                if (!cliforend.fieldByName("ATIVO").asString().equals("S")) {
                    return "erro:Atenção!\nCliente está inativo!";
                }
            }

            TClientDataSet contratoClientePadrao = TClientDataSet.create(vs, "CONTRATOCLIENTEPADRAO");
            contratoClientePadrao.createDataSet();
            contratoClientePadrao.condicao(" WHERE CONTRATOCLIENTEPADRAO.SCONTRATOCLIENTEPADRAO > 0 ");
            contratoClientePadrao.open();
            if (contratoClientePadrao.fieldByName("CLOCAL").isNull()) {
                return "erro:Nenhum local de estoque informado na tela de Dados Padrão para Faturamento do Contrato!";
            } else if (contratoClientePadrao.fieldByName("CMODELONF").isNull()) {
                return "erro:Nenhum modelo de NF na tela de Dados Padrão para Faturamento do Contrato!";
            }

            //<editor-fold defaultstate="collapsed" desc="Criação e alimentação dos clients dataset necessário">
            TSQLDataSetEmp sqlObs = TSQLDataSetEmp.create(vs);

            TClientDataSet contratoClienteNF = TClientDataSet.create(vs, "CONTRATOCLIENTENF");
            contratoClienteNF.createDataSet();
            TClientDataSet contratoClienteLcto = TClientDataSet.create(vs, "CONTRATOCLIENTELCTO");
            contratoClienteLcto.createDataSet();
            contratoClienteLcto.condicao(" WHERE CONTRATOCLIENTE.CCONTRATOCLIENTE = " + cod);
            contratoClienteLcto.open();
            TClientDataSet filial = TClientDataSet.create(vs, "FILIAL");
            filial.createDataSet();
            filial.condicao(" WHERE FILIAL.CFILIAL = " + vs.getParameter("filial"));
            filial.open();

            TClientDataSet ciof = TClientDataSet.create(vs, "CIOF");
            ciof.createDataSet();
            ciof.condicao(" WHERE CIOF.CIOF = " + contratoCliente.fieldByName("CIOF").asString());
            ciof.open();

            TClientDataSet modeloNF = TClientDataSet.create(vs, "MODELONF");
            modeloNF.createDataSet();
            modeloNF.condicao(" WHERE MODELONF.CMODELONF = " + (!ciof.fieldByName("CMODELONF").isNull() ? ciof.fieldByName("CMODELONF").asInteger() : contratoClientePadrao.fieldByName("CMODELONF").asInteger()));
            modeloNF.open();
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Insert NFSAIDA">
            Date dataNF = new Date();

            GeraLivros geraLivros = new GeraLivros();
            try {
                if (!geraLivros.CHECARDATA(vs, dataNF, "S")) {
                    return "abort";
                }
            } catch (ExcecaoTecnicon ex) {
                return "erro:" + ex.getMessage();
            }

            TClientDataSet nfSaida = TClientDataSet.create(vs, "NFSAIDA");
            nfSaida.createDataSet();

            nfSaida.insert();
            nfSaida.fieldByName("CCLIFOR").asInteger(CCLIFOR);
            nfSaida.fieldByName("FILIALCF").asInteger(FILIALCF);
            nfSaida.fieldByName("CIOF").asInteger(contratoCliente.fieldByName("CIOF").asInteger());
            nfSaida.fieldByName("CCUSTO").asInteger(contratoCliente.fieldByName("CCUSTO").asInteger());
            nfSaida.fieldByName("CLOCAL").asInteger(contratoClientePadrao.fieldByName("CLOCAL").asInteger());
            nfSaida.fieldByName("CFILIAL").asInteger(Funcoes.strToInt(vs.getValor("filial")));
            nfSaida.fieldByName("UF").asString(cliforend.fieldByName("NUF1").asString());
            nfSaida.fieldByName("CFOP").asInteger(5303);
            nfSaida.fieldByName("DATA").asDate(Funcoes.strToDate(vs, vs.getParameter("DTFATURAMENTO")));// SOL 127344
            nfSaida.fieldByName("CMODELONF").asInteger(modeloNF.fieldByName("CMODELONF").asInteger());
            nfSaida.fieldByName("SERIE").asString(modeloNF.fieldByName("SERIE").asString());
            nfSaida.fieldByName("SUB").asString(modeloNF.fieldByName("SUB").asString());

            //Valida CidadePrestacaoServico
            if (ciof.fieldByName("ENTRASAI").asString().equals("S")
                    && ciof.fieldByName("CUSTOMO").asString().equals("S")
                    && !modeloNF.fieldByName("NFSE").asString().equals("N")
                    && !"".equals(regra[1])) {
                nfSaida.fieldByName("CCIDADESERVICO").asString(regra[1]);
            }
            String NF;
            Object NFSaida = TecniconLookup.lookup("NfSaida", "FuncoesNF");
            try {
                NF = (String) NFSaida.getClass().getMethod("FNUMERONF", VariavelSessao.class, String.class, Integer.class, String.class, String.class)
                        .invoke(NFSaida, vs, modeloNF.fieldByName("CMODELONF").asString(), 1, "S", "0");
            } catch (NoSuchMethodException | SecurityException | ExcecaoTecnicon | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new ExcecaoTecnicon(vs, ex);
            }
            nfSaida.fieldByName("NF").asString(NF);
            nfSaida.fieldByName("NF1").asString(NF);

            nfSaida.fieldByName("VALOR_TOTAL").asDouble(0);

            TClientDataSet CDSCOM22 = TClientDataSet.create(vs);

            Object objeto = TecniconLookup.lookup("NfSaida", "FuncoesNF");

            nfSaida.post();

            String NFS = nfSaida.fieldByName("NFS").asString();
            contratoClienteNF.insert();
            contratoClienteNF.fieldByName("CCONTRATOCLIENTE").asInteger(cod);
            contratoClienteNF.fieldByName("NFS").asInteger(Integer.parseInt(NFS));
            contratoClienteNF.post();
            //contratoClienteNF.next();
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Insert NFSITEM">
            TClientDataSet PRODUTO = TClientDataSet.create(vs, "PRODUTO");
            PRODUTO.createDataSet();

            TClientDataSet IPI = TClientDataSet.create(vs, "IPI");
            IPI.createDataSet();
            TClientDataSet NFSITEM = TClientDataSet.create(vs, "NFSITEM");
            NFSITEM.createDataSet();

            NfsImpostoItem impostoItem = new NfsImpostoItem();
            Boolean BaseIpiSomarFrete;

            // Somar valor do frete na base do IPI?
            BaseIpiSomarFrete = !"N".equals(regra[0]);

            String ano = Funcoes.copy(vs, vs.getParameter("ANOMES"), 0, 4);
            String mes = Funcoes.copy(vs, vs.getParameter("ANOMES"), 5, 2);
            new ExcecaoTecnicon(vs, "passou 14", new Throwable(), true);
            TSQLDataSetEmp CONDOMINIORATEIOVLR = TSQLDataSetEmp.create(vs);
            CONDOMINIORATEIOVLR.createDataSet();

            CONDOMINIORATEIOVLR.commandText("SELECT CONTRATOVIGENCIA.DATA AS MESANO, "
                    + " CONTRATOVIGENCIA.CCONTRATOCLIENTE,CONTRATOOBJ.CPRODUTO,"
                    + " '' AS CONTA, '' AS NOME,"
                    + "       (CONTRATOOBJETO.UNITARIO * CONTRATOOBJETO.QTDE)AS VALOR"
                    + " FROM CONTRATOVIGENCIA"
                    + " INNER JOIN CONTRATOOBJETO ON (CONTRATOVIGENCIA.CCONTRATOVIGENCIA = CONTRATOOBJETO.CCONTRATOVIGENCIA)"
                    + " INNER JOIN CONTRATOOBJ ON (CONTRATOOBJ.CCONTRATOOBJ = CONTRATOOBJETO.CCONTRATOOBJ)"
                    + " WHERE CONTRATOVIGENCIA.CCONTRATOCLIENTE =" + cod
                    + "   AND CONTRATOVIGENCIA.DTFINAL >= '" + Funcoes.formatarDB(("01/" + mes + "/" + ano), "D") + "'"
                    + "   AND CONTRATOVIGENCIA.DTINICIAL <= '" + Funcoes.formatarDB("" + Funcoes.ultimoDia(vs, mes, ano), "D") + "'");
            CONDOMINIORATEIOVLR.open();
            new ExcecaoTecnicon(vs, "passou record " + CONDOMINIORATEIOVLR.recordCount() + "\r\n SELECT CONTRATOVIGENCIA.DATA AS MESANO, "
                    + " CONTRATOVIGENCIA.CCONTRATOCLIENTE,CONTRATOOBJ.CPRODUTO,"
                    + " '' AS CONTA, '' AS NOME,"
                    + "       (CONTRATOOBJETO.UNITARIO * CONTRATOOBJETO.QTDE)AS VALOR"
                    + " FROM CONTRATOVIGENCIA"
                    + " INNER JOIN CONTRATOOBJETO ON (CONTRATOVIGENCIA.CCONTRATOVIGENCIA = CONTRATOOBJETO.CCONTRATOVIGENCIA)"
                    + " INNER JOIN CONTRATOOBJ ON (CONTRATOOBJ.CCONTRATOOBJ = CONTRATOOBJETO.CCONTRATOOBJ)"
                    + " WHERE CONTRATOVIGENCIA.CCONTRATOCLIENTE =" + cod
                    + "   AND CONTRATOVIGENCIA.DTFINAL >= '" + Funcoes.formatarDB(("01/" + mes + "/" + ano), "D") + "'"
                    + "   AND CONTRATOVIGENCIA.DTINICIAL <= '" + Funcoes.formatarDB("" + Funcoes.ultimoDia(vs, mes, ano), "D") + "'", new Throwable(), true);
            while (!CONDOMINIORATEIOVLR.eof()) {
                NFSITEM.insert();

                PRODUTO.close();
                PRODUTO.condicao(" WHERE PRODUTO.CPRODUTO=" + CONDOMINIORATEIOVLR.fieldByName("CPRODUTO").asString());
                PRODUTO.open();
                IPI.close();
                IPI.condicao(" WHERE IPI.CIPI = " + PRODUTO.fieldByName("CIPI").asString());
                IPI.open();
                NFSITEM.fieldByName("CPRODUTO").asString(CONDOMINIORATEIOVLR.fieldByName("CPRODUTO").asString());
                NFSITEM.fieldByName("CIOF").asString(contratoCliente.fieldByName("CIOF").asString());

                NFSITEM.fieldByName("QTDECLIENTE").asInteger(1);
                NFSITEM.fieldByName("QTDE").asInteger(1);
                NFSITEM.fieldByName("UNITARIOCLI").asDouble(CONDOMINIORATEIOVLR.fieldByName("VALOR").asDouble());
                NFSITEM.fieldByName("UNSDESC").asDouble(CONDOMINIORATEIOVLR.fieldByName("VALOR").asDouble());
                NFSITEM.fieldByName("NFS").asString(NFS);
                NFSITEM.fieldByName("CLOCAL").asString(contratoClientePadrao.fieldByName("CLOCAL").asString());
                NFSITEM.fieldByName("CCUSTO").asString(contratoCliente.fieldByName("CCUSTO").asString());
                NFSITEM.fieldByName("CFOP").asString(nfSaida.fieldByName("CFOP").asString());
                NFSITEM.fieldByName("VDESC").asDouble(
                        (NFSITEM.fieldByName("UNSDESC").asDouble()
                        * NFSITEM.fieldByName("PDESC").asDouble()) / 100d
                );
                NFSITEM.fieldByName("UNITARIO").asDouble(
                        NFSITEM.fieldByName("UNSDESC").asDouble()
                        - NFSITEM.fieldByName("VDESC").asDouble()
                );
                NFSITEM.fieldByName("CIPI").asString(PRODUTO.fieldByName("CIPI").asString());
                NFSITEM.fieldByName("STIPI").asString(IPI.fieldByName("STS").asString());
                //NFSITEM.fieldByName("TOTAL").asDouble(NFSITEM.fieldByName("QTDECLIENTE").asDouble() * NFSITEM.fieldByName("UNITARIO").asDouble());
                Integer IPITAG = 0;
                calcularNFSItem(NFSITEM, nfSaida, vs, IPITAG, PRODUTO, impostoItem, BaseIpiSomarFrete);
                NFSITEM.post();
                new ExcecaoTecnicon(vs, "passou 16", new Throwable(), true);
                CONDOMINIORATEIOVLR.next();
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Finalizar NF Saida">
            TClientDataSet nfsTotal = TClientDataSet.create(vs, "NFSTOTAL");
            nfsTotal.createDataSet();
            nfsTotal.commandText(nfsTotal.commandText().replace("{NFS}", nfSaida.fieldByName("NFS").asString()));
            nfsTotal.condicao("WHERE 1=1");
            nfsTotal.open();

            NFSITEM.close();
            NFSITEM.condicao("WHERE NFSITEM.NFS = " + nfSaida.fieldByName("NFS").asString());
            NFSITEM.open();

            NFSITEM.first();
            Double TDESPESANF;
            TDESPESANF = 0.0;
            double TSEGURONF = 0.0;
            double TFRETENF = 0.0;
            while (!NFSITEM.eof()) {
                NFSITEM.edit();

                if (nfSaida.fieldByName("DESCONTO").asDouble() != 0) {
                    NFSITEM.fieldByName("DESCONTONF").asDouble((Funcoes.round(NFSITEM.fieldByName("TOTAL").asDouble()
                            * ((nfSaida.fieldByName("DESCONTO").asDouble()) * 100 / nfsTotal.fieldByName("SMERC").asDouble()))) / 100);
                }
                if (((nfSaida.fieldByName("FRETE").asDouble() + nfSaida.fieldByName("SEGURO").asDouble() + nfSaida.fieldByName("DESPESA").asDouble()) != 0)
                        && (nfsTotal.fieldByName("SMERC").asDouble() != 0)) {
                    NFSITEM.fieldByName("DESPESANF").asDouble((Funcoes.round(NFSITEM.fieldByName("TOTAL").asDouble() * ((nfSaida.fieldByName("DESPESA").asDouble()) * 100
                            / nfsTotal.fieldByName("SMERC").asDouble()))) / 100);
                    NFSITEM.fieldByName("FRETENF").asDouble((Funcoes.round(NFSITEM.fieldByName("TOTAL").asDouble() * ((nfSaida.fieldByName("FRETE").asDouble()) * 100
                            / nfsTotal.fieldByName("SMERC").asDouble()))) / 100);
                    NFSITEM.fieldByName("SEGURONF").asDouble((Funcoes.round(NFSITEM.fieldByName("TOTAL").asDouble() * ((nfSaida.fieldByName("SEGURO").asDouble()) * 100
                            / nfsTotal.fieldByName("SMERC").asDouble()))) / 100);
                    TDESPESANF = TDESPESANF + NFSITEM.fieldByName("DESPESANF").asDouble();
                    TFRETENF = TFRETENF + NFSITEM.fieldByName("FRETENF").asDouble();
                    TSEGURONF = TSEGURONF + NFSITEM.fieldByName("SEGURONF").asDouble();
                    if (TDESPESANF > nfSaida.fieldByName("DESPESA").asDouble()) {
                        NFSITEM.fieldByName("DESPESANF").asDouble(0.00);
                    }
                    if (TFRETENF > nfSaida.fieldByName("FRETE").asDouble()) {
                        NFSITEM.fieldByName("FRETENF").asDouble(0.00);
                    }
                    if (TSEGURONF > nfSaida.fieldByName("SEGURO").asDouble()) {
                        NFSITEM.fieldByName("SEGURONF").asDouble(0.00);
                    }
                }

                if (nfSaida.fieldByName("ACRESCIMO").asDouble() != 0 && nfsTotal.fieldByName("SMERC").asDouble() != 0) {
                    NFSITEM.fieldByName("ACRESCIMONF").asDouble((Funcoes.round(NFSITEM.fieldByName("TOTAL").asDouble()
                            * (nfSaida.fieldByName("ACRESCIMO").asDouble() * 100 / nfsTotal.fieldByName("SMERC").asDouble()))) / 100);
                }
                NFSITEM.post();
                NFSITEM.next();
            }

            vs.addParametros("NFS", NFS);
            Object NFSitem = TecniconLookup.lookup("NfsItem", "NfsItem");
            try {
                String retorno2 = (String) NFSitem.getClass().getMethod("calculaDadosExtrasFull", VariavelSessao.class, boolean.class, boolean.class, boolean.class).invoke(NFSitem, vs, false, false, false);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

                throw new ExcecaoTecnicon(vs, ex);
            }

            nfSaida.close();
            nfSaida.condicao(" WHERE NFSAIDA.NFS=" + NFS);
            nfSaida.open();

            sqlObs.close();
            sqlObs.commandText("SELECT PRODUTO.DESCRICAO,NFSITEM.TOTAL"
                    + " FROM NFSITEM"
                    + " INNER JOIN PRODUTO ON (PRODUTO.CPRODUTO = NFSITEM.CPRODUTO)"
                    + " WHERE NFSITEM.NFS=" + NFS);
            sqlObs.open();

            StringBuilder OBS = new StringBuilder();
            int maiorDescricao = 0;
            while (!sqlObs.eof()) {

                if ((sqlObs.fieldByName("DESCRICAO").asString() + sqlObs.fieldByName("TOTAL").asString()).length() > maiorDescricao) {
                    maiorDescricao = (sqlObs.fieldByName("DESCRICAO").asString() + sqlObs.fieldByName("TOTAL").asString()).length();
                }
                sqlObs.next();
            }

            sqlObs.first();
            maiorDescricao += 10;
            while (!sqlObs.eof()) {
                OBS.append(sqlObs.fieldByName("DESCRICAO").asString());
                OBS.append(Funcoes.carn(vs, Funcoes.formatFloat("#0.00", sqlObs.fieldByName("TOTAL").asDouble()),
                        maiorDescricao - sqlObs.fieldByName("DESCRICAO").asString().length(), "D", " ", "", 2));

                sqlObs.next();
                if (!sqlObs.eof()) {
                    OBS.append("\n");
                }
            }

            objeto = TecniconLookup.lookup("NfsImpFon", "NfsImpFon");

            try {
                objeto.getClass().getMethod("validacao", VariavelSessao.class).invoke(objeto, vs);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                return "erro:" + ex.getMessage();
            }

            try {
                vs.addParametros("OBSRECEBER", OBS.toString());
                StringBuilder sb = new StringBuilder();
                TClassLoader.execMethod("NfsReceber/NfsReceber", "NFSAIDAGERADUPL", NFS, false, vs, contratoCliente.fieldByName("CPRAZO").asInteger(), contratoCliente.fieldByName("CCARTEIRA").asInteger(), sb);
                vs.addParametros("CDSRECEBER", null);
            } catch (ExcecaoTecnicon ex) {
                throw ex;
            }

            vs.addParametros("codigo", NFS);
            objeto = TecniconLookup.lookup("NfsSubConta", "NfsSubConta");

            try {
                objeto.getClass().getMethod("validacao", VariavelSessao.class).invoke(objeto, vs);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                return "erro:" + ex.getMessage();
            }

            objeto = TecniconLookup.lookup("LFsICMS", "LFsICMS");

            try {
                objeto.getClass().getMethod("validacao", VariavelSessao.class).invoke(objeto, vs);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                return "erro:" + ex.getMessage();
            }

            objeto = TecniconLookup.lookup("LFSIPI", "LFSIPI");

            try {
                objeto.getClass().getMethod("validacao", VariavelSessao.class).invoke(objeto, vs);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                return "erro:" + ex.getMessage();
            }
            //</editor-fold>

            return "Nota fiscal " + NFS + " gerada para o contrato " + cod;
        } catch (ExcecaoTecnicon e) {
            throw new ExcecaoTecnicon(vs, "ero1 = " + e.getMessage(), e, true);
        }
    }

    private void calcularNFSItem(TClientDataSet NFSITEM, TClientDataSet nfSaida, VariavelSessao vs, Integer IPITAG, TClientDataSet PRODUTO, NfsImpostoItem impostoItem, Boolean BaseIpiSomarFrete) throws ExcecaoTecnicon {
        UDmCes.CE_CALCULATOTALPDNF(NFSITEM, "V", "", false, nfSaida, vs);
        UDmCes.CE_RETORNAPIPI(NFSITEM.fieldByName("CPRODUTO").asInteger(), NFSITEM.fieldByName("CIOF").asInteger(), NFSITEM.fieldByName("TOTAL").asDouble(),
                NFSITEM.fieldByName("IPI"), NFSITEM.fieldByName("STIPI"), NFSITEM.fieldByName("PBIPI"), NFSITEM.fieldByName("BIPI"), -1.0,
                nfSaida.fieldByName("CFILIAL").asInteger(), nfSaida.fieldByName("CCLIFOR").asInteger(), nfSaida.fieldByName("FILIALCF").asInteger(), vs, IPITAG);
        NFSITEM.fieldByName("PRECODIA").asDouble(TecniRetorna.VERPRECO(PRODUTO.fieldByName("CPRODUTO").asString(), vs));
        impostoItem.impostoItems("V", 0, 0, 0, BaseIpiSomarFrete, NFSITEM, vs);
    }

    public void formataClient(TClientDataSet cdsGrid) throws ExcecaoTecnicon {
        cdsGrid.fieldDefs().get(cdsGrid.fieldDefs().findField("CB")).setTIPO("CHECKBOXV");
        cdsGrid.fieldDefs().get(cdsGrid.fieldDefs().findField("CB")).setLABEL("");

        cdsGrid.fieldDefs().get(cdsGrid.fieldDefs().findField("CCONTRATOCLIENTE")).setLABEL("Cód. Contrato");
        cdsGrid.fieldDefs().get(cdsGrid.fieldDefs().findField("CCONTRATOCLIENTE")).setCHAVE("F");
        cdsGrid.fieldDefs().get(cdsGrid.fieldDefs().findField("CCONTRATOCLIENTE")).setTABELAORIGEM("CONTRATOCLIENTE");
        cdsGrid.fieldDefs().get(cdsGrid.fieldDefs().findField("CCONTRATOCLIENTE")).setALIASTABELA("CONTRATOCLIENTE_1");
        cdsGrid.fieldDefs().get(cdsGrid.fieldDefs().findField("CCONTRATOCLIENTE")).setCAMPOORIGEM("CCONTRATOCLIENTE");

        cdsGrid.fieldDefs().get(cdsGrid.fieldDefs().findField("CCLIFOR")).setLABEL("Cód. cliente");
        cdsGrid.fieldDefs().get(cdsGrid.fieldDefs().findField("FILIALCF")).setLABEL("Filial");
        cdsGrid.fieldDefs().get(cdsGrid.fieldDefs().findField("NOMEFILIAL")).setLABEL("Cliente");
        cdsGrid.fieldDefs().get(cdsGrid.fieldDefs().findField("CFILIAL")).setLABEL("Cód filial");
        cdsGrid.fieldDefs().get(cdsGrid.fieldDefs().findField("DTINICIAL")).setLABEL("Data inicial");
        cdsGrid.fieldDefs().get(cdsGrid.fieldDefs().findField("DTFINAL")).setLABEL("Data final");
        cdsGrid.fieldDefs().get(cdsGrid.fieldDefs().findField("VALOR")).setLABEL("Valor Fatura");
    }
}
