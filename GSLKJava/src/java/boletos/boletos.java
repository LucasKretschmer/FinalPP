package boletos;

import br.com.tecnicon.server.dataset.TSQLDataSetEmp;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import java.util.Date;
import javax.ejb.Stateless;
import org.json.JSONObject;

/**
 *
 * @author Lucas Kretschmer
 */
@Stateless
public class boletos {

    public String retornaDuplicatas(VariavelSessao vs) throws ExcecaoTecnicon {
        String dadosCli = vs.getParameter("CCLIFOR");
        JSONObject obj = new JSONObject();
        obj.put("STATUS", "OK");
        StringBuilder html = new StringBuilder();
        TSQLDataSetEmp dupl = TSQLDataSetEmp.create(vs);
        dupl.commandText("SELECT "
                + "	RECEBER.SRECEBER, "
                + "	RECEBER.DUPLICATA, "
                + "	RECEBER.DATA, "
                + "	RECEBER.VCTO, "
                + "	RECEBER.OBS, "
                + "	LOTEBXRECEBER.DATA AS DTPAGAMENTO, "
                + "	CASE WHEN (COALESCE(TDATEDIFF(COALESCE(LOTEBXRECEBER.DATA,TCURRENT_DATE()),RECEBER.VCTO),0)) < 0 THEN 0 ELSE COALESCE(TDATEDIFF(COALESCE(LOTEBXRECEBER.DATA,TCURRENT_DATE()),RECEBER.VCTO),0) END AS DIASATRASO, "
                + "	BXRECEBER.SPAGO, "
                + "	RECEBER.PARCELA, "
                + "	TPBXCR.NOME, "
                + "	LOTEBXRECEBER.LOTEBXREC, "
                + "	LOTEBXRECEBER.CFILIAL, "
                + "	BXRECEBER.SJURO, "
                + "	BXRECEBER.SDESPESA, "
                + "	BXRECEBER.SDESCONTO "
                + "FROM RECEBER "
                + "LEFT JOIN BXRECEBER BX ON (BX.sreceber = RECEBER.sreceber) "
                + "LEFT JOIN NFSAIDA ON (NFSAIDA.NFS = RECEBER.NFS) "
                + "LEFT JOIN LOTEBXRECEBER ON (LOTEBXRECEBER.lotebxrec = BX.lotebxrec) "
                + "LEFT JOIN TPBXCR ON (TPBXCR.CTPBXCR = LOTEBXRECEBER.CTPBXCR) "
                + "LEFT JOIN CLIFOR ON (RECEBER.CCLIFOR = CLIFOR.CCLIFOR) "
                + "LEFT JOIN(SELECT "
                + "        BXRECEBER.LOTEBXREC, "
                + "        SUM(BXRECEBER.VALOR) SVALOR, "
                + "        SUM(BXRECEBER.JURO) SJURO, "
                + "        SUM(BXRECEBER.DESCONTO) SDESCONTO, "
                + "        SUM(BXRECEBER.DESPESA) SDESPESA, "
                + "        SUM(BXRECEBER.TARIFA) STARIFA, "
                + "        SUM(BXRECEBER.ADIANTAMENTO) SADIANTAMENTO, "
                + "        SUM(BXRECEBER.PISRETIDO) SPISRETIDO, "
                + "        SUM(BXRECEBER.COFINSRETIDO) SCOFINSRETIDO, "
                + "        SUM(BXRECEBER.PAGO) SPAGO "
                + "    FROM BXRECEBER "
                + "    GROUP BY BXRECEBER.LOTEBXREC) BXRECEBER ON (BXRECEBER.LOTEBXREC = LOTEBXRECEBER.LOTEBXREC) "
                + "WHERE CLIFOR.CCLIFOR = " + dadosCli
                + "AND RECEBER.CFILIAL = 1 "
                + "GROUP BY RECEBER.DUPLICATA,RECEBER.OBS, COALESCE(NFSAIDA.NF,NFSAIDA.NF1),RECEBER.DATA,RECEBER.VCTO, "
                + "RECEBER.VCTOP,RECEBER.DATA,BXRECEBER.LOTEBXREC,LOTEBXRECEBER.DATA,RECEBER.PARCELA, "
                + "TPBXCR.NOME, LOTEBXRECEBER.LOTEBXREC,LOTEBXRECEBER.CFILIAL,BXRECEBER.SPAGO,BXRECEBER.SJURO,BXRECEBER.SDESPESA, "
                + "BXRECEBER.SDESCONTO,RECEBER.SRECEBER "
                + "ORDER BY RECEBER.VCTO ");
        dupl.open();
        dupl.first();
        while (!dupl.eof()) {
            if (dupl.fieldByName("SPAGO").asInteger() != 0) {
                //Pagas
                html.append("<tr>\n"
                        + "  <td class=\"TMcod\">" + dupl.fieldByName("SRECEBER").asString() + "</td>\n"
                        + "  <td class=\"TMDtVencimento\">" + dupl.fieldByName("VCTO").asString() + "</td>\n"
                        + "  <td class=\"TMStatus\"><span class=\"lquidado\">Liquidado</span></td>\n"
                        + "  <td class=\"TMDtLiquidado\">" + dupl.fieldByName("DTPAGAMENTO").asString() + "</td>\n"
                        + "  <td class=\"TMDescricao\"> " + dupl.fieldByName("OBS").asString() + "</td>\n"
                        + "  <td class=\"TMpdf\"><span class=\"btnPDF\" sreceber=\"" + dupl.fieldByName("SRECEBER").asString() + "\"><i class=\"fas fa-file-pdf\"></i></span></td>"
                        + "</tr>");
                dupl.next();
            } else if (Funcoes.compararData(dupl.fieldByName("VCTO").asDate(), "<=", new Date()) && dupl.fieldByName("SPAGO").asInteger() == 0) {
                //Pendentes
                html.append("<tr>\n"
                        + "  <td class=\"TMcod\">" + dupl.fieldByName("SRECEBER").asString() + "</td>\n"
                        + "  <td class=\"TMDtVencimento\">" + dupl.fieldByName("VCTO").asString() + "</td>\n"
                        + "  <td class=\"TMStatus\"><span class=\"pendente\">Pendente</span></td>\n"
                        + "  <td class=\"TMDtLiquidado\">" + dupl.fieldByName("DTPAGAMENTO").asString() + "</td>\n"
                        + "  <td class=\"TMDescricao\"> " + dupl.fieldByName("OBS").asString() + "</td>\n"
                        + "  <td class=\"TMpdf\"><span class=\"btnPDF\" sreceber=\"" + dupl.fieldByName("SRECEBER").asString() + "\"><i class=\"fas fa-file-pdf\"></i></span></td>"
                        + "</tr>");
                dupl.next();
            } else {
                //resto
                html.append("<tr>\n"
                        + "  <td class=\"TMcod\">" + dupl.fieldByName("SRECEBER").asString() + "</td>\n"
                        + "  <td class=\"TMDtVencimento\">" + dupl.fieldByName("VCTO").asString() + "</td>\n"
                        + "  <td class=\"TMStatus\"><span class=\"atrasado\">Pendente</span></td>\n"
                        + "  <td class=\"TMDtLiquidado\">" + dupl.fieldByName("DTPAGAMENTO").asString() + "</td>\n"
                        + "  <td class=\"TMDescricao\"> " + dupl.fieldByName("OBS").asString() + "</td>\n"
                        + "  <td class=\"TMpdf\"><span class=\"btnPDF\" sreceber=\"" + dupl.fieldByName("SRECEBER").asString() + "\"><i class=\"fas fa-file-pdf\"></i></span></td>"
                        + "</tr>");
                dupl.next();
            }
        }
        //obj.put("BOLETOS", html.toString());
        return html.toString();
    }
}
