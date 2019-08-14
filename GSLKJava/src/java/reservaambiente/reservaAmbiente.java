/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reservaambiente;

import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.dataset.TSQLDataSetEmp;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import java.util.Date;
import javax.ejb.Stateless;

/**
 *
 * @author Lucas Kretschmer
 */
@Stateless
public class reservaAmbiente {

    public String comboSala(VariavelSessao vs) throws ExcecaoTecnicon {
        try {
            String html = "";
            TSQLDataSetEmp sql = TSQLDataSetEmp.create(vs);
            sql.commandText(" SELECT "
                    + "	   LOCALVISITA.*, "
                    + "    PREDIO.PREDIO, "
                    + "	   TPLOCALVISITA.TPLOCALVISITA "
                    + " FROM LOCALVISITA "
                    + " LEFT OUTER JOIN PREDIO ON (PREDIO.CPREDIO = LOCALVISITA.CPREDIO) "
                    + " LEFT OUTER JOIN TPLOCALVISITA ON (TPLOCALVISITA.CTPLOCALVISITA = LOCALVISITA.CTPLOCALVISITA) "
                    + " WHERE LOCALVISITA.STATUS = 'L' "
                    + " ORDER BY LOCALVISITA.LOCALVISITA ");
            sql.open();

            html += " <select id=\"grauDependente\" class=\"basic divinput input-padrao\" disabled>\n";
            while (!sql.eof()) {
                html += "     <option value=\"" + sql.fieldByName("CLOCALVISITA").asInteger() + "\" >" + sql.fieldByName("LOCALVISITA").asString() + "</option>\n";
                sql.next();
            }
            html += " </select> ";

            return html;
        } catch (Exception e) {
            throw new ExcecaoTecnicon(vs, "Ocorreu um erro: " + e.getMessage(), e, true);
        }
    }

    public String reservarLocal(VariavelSessao vs) throws ExcecaoTecnicon {
        String titulo = vs.getParameter("TITULO");
        String cfunc = vs.getParameter("CFUNC");
        String cLocalVisita = vs.getParameter("CLOCALVISITA");
        Date data = Funcoes.strToDate(vs, vs.getParameter("DATA"));
        String hrIni = vs.getParameter("HRINI");
        String hrFim = vs.getParameter("HRFIM");

        TClientDataSet reserva = TClientDataSet.create(vs, "RESERVALOCALCLIFOR");
        reserva.createDataSet();
        reserva.condicao(" WHERE RESERVALOCALCLIFOR.DATA = '" + data + "' "
                + " AND '" + hrIni + "' BETWEEN RESERVALOCALCLIFOR.HRINI AND RESERVALOCALCLIFOR.HRFIM "
                + " AND '" + hrFim + "' BETWEEN RESERVALOCALCLIFOR.HRINI AND RESERVALOCALCLIFOR.HRFIM "
                + " AND RESERVALOCALCLIFOR.CLOCALVISITA = " + cLocalVisita);
        reserva.open();

        if (reserva.isEmpty()) {
            reserva.insert();
            reserva.fieldByName("CRESERVALOCALCLIFOR").asString(cLocalVisita);
            reserva.fieldByName("CCLIFOR").asString(cfunc);
            reserva.fieldByName("TITULO").asString(titulo);
            reserva.fieldByName("DATA").asDate(data);
            reserva.fieldByName("HRINI").asString(hrIni);
            reserva.fieldByName("HRFIM").asString(hrFim);
            reserva.post();

            return "Reserva Efetuada!";
        } else {
            return "Já exite uma reserva para esta data e hora para este estabelecimento!";
        }
    }

    public Date incrementarDate(Date data, String tpRepetir) {
        if (tpRepetir.equals("1"))//Diariamente{
        {
            data = Funcoes.incDay(data, 1);
        } else if (tpRepetir.equals("2"))//Todos os dias úteis(Seg-Sex)
        {
            data = Funcoes.incDay(data, 1);
            if (!Funcoes.isDiaUtil(data)) {
                data = incrementarDate(data, tpRepetir);
            }
        } else if (tpRepetir.equals("3"))//Semanalmente
        {
            data = Funcoes.incDay(data, 7);
        } else if (tpRepetir.equals("4"))//Cada 2 Semanas
        {
            data = Funcoes.incDay(data, 14);
        } else if (tpRepetir.equals("5"))//Mensalmente
        {
            data = Funcoes.incMonth(data, 1);
        } else if (tpRepetir.equals("6"))//Anualmente
        {
            data = Funcoes.incYear(data, 1);
        }
        return data;
    }
}
