/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calendarioReserva;

import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.dataset.TSQLDataSetEmp;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import java.util.Date;

/**
 *
 * @author Lucas Kretschmer
 */
public class calendarioReserva {

    public String consultaAgenda(VariavelSessao vs) throws ExcecaoTecnicon {
        TClientDataSet XCDS = TClientDataSet.create(vs);
        XCDS.fields().clear();
        XCDS.fields().add("DATA", "FTDATE", 10);
        XCDS.fields().add("MOSTRA", "FTSTRING", 100);
        XCDS.fields().add("TABELA", "FTSTRING", 31);
        XCDS.fields().add("CAMPO", "FTSTRING", 31);
        XCDS.fields().add("CHAVE", "FTSTRING", 31);
        XCDS.fields().add("CRESERVALOCALCLIFOR", "FTINTEGER", 10);
        XCDS.fields().add("HRINI", "FTSTRING", 10);
        XCDS.fields().add("HRFIM", "FTSTRING", 10);
        XCDS.fields().add("COR", "FTSTRING", 10);
        XCDS.createDataSet();

        TSQLDataSetEmp sql = TSQLDataSetEmp.create(vs);
        sql.commandText("SELECT RESERVALOCALCLIFOR.*, "
                + " RESERVALOCALCLIFOR.HRINI, RESERVALOCALCLIFOR.HRFIM, LOCALVISITA.LOCALVISITA, FUNCIONARIO.NOME,"
                + " 'RESERVALOCALCLIFOR' AS TABELA, 'DATA,HRINI,HRFIM' AS CAMPO, 'SRESERVALOCALCLIFOR' AS CHAVE, LOCALVISITA.COR"
                + " FROM RESERVALOCALCLIFOR"
                + "     INNER JOIN LOCALVISITA ON (LOCALVISITA.CLOCALVISITA = RESERVALOCALCLIFOR.CLOCALVISITA)"
                + "     INNER JOIN FUNCIONARIO ON (FUNCIONARIO.CFUNC = RESERVALOCALCLIFOR.CFUNC)"
                + " WHERE RESERVALOCALCLIFOR.DATA >= '" + Funcoes.formatarDB(Funcoes.dateToStr(new Date()), "D") + "'"
                + " ORDER BY RESERVALOCALCLIFOR.DATA, RESERVALOCALCLIFOR.HRINI");
        sql.open();

        if (!sql.isEmpty()) {
            while (!sql.eof()) {
                XCDS.insert();
                XCDS.fieldByName("DATA").asDate(sql.fieldByName("DATA").asDate());
                XCDS.fieldByName("MOSTRA").asString(sql.fieldByName("HRINI").asString() + " - " + sql.fieldByName("LOCALVISITA").asString() + " (" + sql.fieldByName("NOME").asString() + ")");
                XCDS.fieldByName("TABELA").asString(sql.fieldByName("TABELA").asString());
                XCDS.fieldByName("CAMPO").asString(sql.fieldByName("CAMPO").asString());
                XCDS.fieldByName("CHAVE").asString(sql.fieldByName("CHAVE").asString());
                XCDS.fieldByName("CRESERVALOCALCLIFOR").asString(sql.fieldByName("CRESERVALOCALCLIFOR").asString());
                XCDS.fieldByName("HRINI").asString(sql.fieldByName("HRINI").asString());
                XCDS.fieldByName("HRFIM").asString(sql.fieldByName("HRFIM").asString());
                XCDS.fieldByName("COR").asString(sql.fieldByName("COR").asString());
                XCDS.post();
                sql.next();
            }
        }

        return XCDS.jsonData();
    }

}
