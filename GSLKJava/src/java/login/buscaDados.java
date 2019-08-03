/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package login;

import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.dataset.TSQLDataSetEmp;
import br.com.tecnicon.server.execoes.ExcecaoMsg;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import javax.ejb.Stateless;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Lucas Kretschmer
 */
@Stateless
public class buscaDados {

    public String buscaDados(VariavelSessao vs) throws ExcecaoTecnicon {
        Funcoes.validaVSNNNome(vs, new String[]{
            "COD", "Código do Usuário"
        });

        JSONObject jData = new JSONObject();

        TClientDataSet cdsCliforend = TClientDataSet.create(vs, "CLIFOREND");
        cdsCliforend.createDataSet();
        cdsCliforend.condicao("WHERE CLIFOREND.CCLIFOR = " + vs.getParameter("COD"));
        cdsCliforend.open();

        TClientDataSet cdsClifor = TClientDataSet.create(vs, "CLIFOR");
        cdsClifor.createDataSet();
        cdsClifor.condicao("WHERE CLIFOR.CCLIFOR = " + vs.getParameter("COD"));
        cdsClifor.open();

        TClientDataSet cdsCliforenduser = TClientDataSet.create(vs, "CLIFORENDUSER");
        cdsCliforenduser.createDataSet();
        cdsCliforenduser.condicao("WHERE CLIFORENDUSER.EMAIL = '" + cdsCliforend.fieldByName("EMAIL").asString() + "' ");
        cdsCliforenduser.open();

        if (cdsCliforenduser.isEmpty()) {
            throw new ExcecaoMsg("Usuário inexistente, tente novamente!");
        }
        jData.put("CCLIFOR", cdsClifor.fieldByName("CCLIFOR").asInteger());
        jData.put("NOME", cdsClifor.fieldByName("NOME").asString());
        jData.put("DTCADASTRO", Funcoes.formatarData(cdsClifor.fieldByName("DTCADASTRO").asDate(), "yyyy-MM-dd"));
        jData.put("EMAIL", cdsCliforend.fieldByName("EMAIL").asString());

        if (!cdsCliforend.isEmpty()) {
            jData.put("AVANCADO", true);
            jData.put("CPF", cdsCliforend.fieldByName("CGC").asString());
            jData.put("ENDERECO", cdsCliforend.fieldByName("ENDERECO").asString());
            jData.put("NUMERO", cdsCliforend.fieldByName("NUMERO").asInteger());
            jData.put("BAIRRO", cdsCliforend.fieldByName("BAIRRO").asString());
            jData.put("CIDADE", cdsCliforend.fieldByName("COMPLEMENTO").asString());
            jData.put("CEP", cdsCliforend.fieldByName("CEP").asString());
            jData.put("FONE", cdsCliforend.fieldByName("FONE").asString());
            jData.put("CELULAR", cdsCliforend.fieldByName("CELULAR").asString());

            TSQLDataSetEmp cteContrato = TSQLDataSetEmp.create(vs);
            cteContrato.createDataSet();
            cteContrato.commandText(new StringBuilder()
                    .append(" SELECT GSCONTRATO1.CCONTRATO,")
                    .append("        GSCONTRATO1.DATA,")
                    .append("        GSCONTRATO1.VALOR,")
                    .append("        GSPLANO.NOME AS NOMEPLANO,")
                    .append("        GSPAGAMENTO.NOME AS NOMEPAGAMENTO,")
                    .append("        GSPAGAMENTO.QTDEDIAS,")
                    .append("        GSPLANO.QTDEPESSOAS")
                    .append(" FROM GSCONTRATO1 ")
                    .append(" INNER JOIN GSPLANO ON (GSPLANO.CPLANO = GSCONTRATO1.CPLANO)")
                    .append(" INNER JOIN GSPAGAMENTO ON (GSPAGAMENTO.CPAGAMENTO = GSCONTRATO1.CPAGAMENTO)")
                    .append(" WHERE GSCONTRATO1.CCLIFOR = ").append(vs.getParameter("COD"))
                    .toString());
            cteContrato.open();

            if (!cteContrato.isEmpty()) {
                jData.put("PLANO", true);
                jData.put("CCONTRATO", cteContrato.fieldByName("CCONTRATO").asInteger());
                jData.put("DATACONTRATO", Funcoes.formatarData(cteContrato.fieldByName("DATA").asDate(), "yyyy-MM-dd"));
                jData.put("VALOR", cteContrato.fieldByName("VALOR").asDouble());
                jData.put("NOMEPLANO", cteContrato.fieldByName("NOMEPLANO").asString());
                jData.put("NOMEPAGAMENTO", cteContrato.fieldByName("NOMEPAGAMENTO").asString());
                jData.put("QTDEDIAS", cteContrato.fieldByName("QTDEDIAS").asInteger());
                jData.put("QTDEPESSOAS", cteContrato.fieldByName("QTDEPESSOAS").asInteger());
            }

            return jData.toString();
        }

        jData.put("AVANCADO", false);
        return jData.toString();
    }

    public String buscaTiposPagameno(VariavelSessao vs) throws ExcecaoTecnicon {
        JSONObject jasao;
        JSONArray arai = new JSONArray();
        int i = 0;

        TClientDataSet cdsPaga = TClientDataSet.create(vs, "GSPAGAMENTO");
        cdsPaga.createDataSet();
        cdsPaga.condicao("WHERE 1 = 1");
        cdsPaga.open();
        cdsPaga.first();

        while (!cdsPaga.eof()) {
            jasao = new JSONObject();
            jasao.put("CPAGAMENTO", cdsPaga.fieldByName("CPAGAMENTO").asInteger());
            jasao.put("NOME", cdsPaga.fieldByName("NOME").asString());
            arai.put(i, jasao);
            i++;
            cdsPaga.next();
        }

        return arai.toString();
    }

    public String buscaPromocao(VariavelSessao vs) throws ExcecaoTecnicon {
        JSONObject jsObj = new JSONObject();
        TClientDataSet cdsData = TClientDataSet.create(vs, "GSPLANO");
        cdsData.createDataSet();
        cdsData.condicao("WHERE GSPLANO.CPLANO = " + vs.getParameter("CODPLANO"));
        cdsData.open();
        cdsData.first();

        if (!cdsData.isEmpty()) {
            jsObj.put("STATUS", true);
            if (cdsData.fieldByName("ATIVO").asString().equals("S")) {
                jsObj.put("COD", cdsData.fieldByName("CPLANO").asInteger());
                jsObj.put("NOME", cdsData.fieldByName("NOME").asString());
                jsObj.put("DESC", cdsData.fieldByName("DESCRICAO").asString());
                jsObj.put("QTDEPESSU", cdsData.fieldByName("QTDEPESSOAS").asInteger());
                jsObj.put("VALOR", cdsData.fieldByName("VALORMES").asDouble());
            }
        }
        return jsObj.toString();
    }

    public String buscaFaturas(VariavelSessao vs) {

        return "";
    }
}
