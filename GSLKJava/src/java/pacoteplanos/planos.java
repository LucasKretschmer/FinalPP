package pacoteplanos;

import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import javax.ejb.Stateless;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Lucas Kretschmer
 */
@Stateless
public class planos {

    public String retornarPlanos(VariavelSessao vs) throws ExcecaoTecnicon {
        JSONObject jsObj = new JSONObject();
        JSONArray jsData = new JSONArray();
        TClientDataSet cdsData = TClientDataSet.create(vs, "GSPLANO");
        cdsData.createDataSet();
        cdsData.condicao("WHERE 1 = 1");
        cdsData.open();
        cdsData.first();
        int i = 0;

        if (!cdsData.isEmpty()) {
            jsObj.put("STATUS", "TRUE");
            jsData.put(0, jsObj);
            while (!cdsData.eof()) {
                i += 1;
                jsObj = new JSONObject();
                if (cdsData.fieldByName("ATIVO").asString().equals("S")) {
                    jsObj.put("COD", cdsData.fieldByName("CPLANO").asInteger());
                    jsObj.put("NOME", cdsData.fieldByName("NOME").asString());
                    jsObj.put("DESC", cdsData.fieldByName("DESCRICAO").asString());
                    jsObj.put("QTDEPESSU", cdsData.fieldByName("QTDEPESSOAS").asInteger());
                    jsObj.put("VALOR", cdsData.fieldByName("VALORMES").asDouble());
                    jsData.put(i, jsObj);
                }
                cdsData.next();
            }
        } else {
            jsObj.put("STATUS", "FALSE");
            jsData.put(0, jsObj);
        }

        return jsData.toString();
    }
}
