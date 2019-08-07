package dependentes;

import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.execoes.ExcecaoMsg;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import javax.ejb.Stateless;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author lucas.kretschmer
 */
@Stateless
public class dependentes {

    public String atualizaGrid(VariavelSessao vs) throws ExcecaoTecnicon {
        Funcoes.validaVSNNNome(vs, new String[]{
            "CCLIFOR", "Código do Cliente"
        });
        try {
            int cclifor = Funcoes.strToInt(vs.getParameter("CCLIFOR"));
            int cclifordep;
            String grau;
            String html = "";
            TClientDataSet cdsData = TClientDataSet.create(vs, "CLIFORDEP");
            cdsData.createDataSet();
            cdsData.condicao(" WHERE CLIFORDEP.CCLIFOR = " + cclifor);
            cdsData.open();

            if (!cdsData.isEmpty()) {
                while (!cdsData.eof()) {
                    cclifordep = cdsData.fieldByName("CCLIFORDEP").asInteger();
                    switch (cdsData.fieldByName("GRAU").asInteger()) {
                        case 11:
                            grau = "Marido/Esposa";
                            break;
                        case 21:
                            grau = "Filho/Enteado";
                            break;
                        case 24:
                            grau = "Irmão/Irmã";
                            break;
                        case 31:
                            grau = "Pai/Mãe";
                            break;
                        default:
                            grau = "Parente";
                    }

                    html += "<tr class=\"tdDependentes\" ccliforfilho=\"" + cclifordep + "\" cclifor=\"" + cclifor + "\" >\n"
                            + " <td>" + cdsData.fieldByName("NOME").asString() + "</td>\n"
                            + " <td>" + grau + "</td>\n"
                            + " <td>" + cdsData.fieldByName("DTNASC").asString() + "</td>\n"
                            + "</tr>\n";
                    cdsData.next();
                }
            } else {
                html += "<tr>"
                        + "<td></td>"
                        + "<td>Sem dependentes</td>"
                        + "<td></td>"
                        + "<tr>";
            }

            return html;
        } catch (Exception e) {
            throw new ExcecaoMsg(vs, e.getMessage());
        }
    }

    public String buscaDadosDep(VariavelSessao vs) throws ExcecaoTecnicon {
        try {
            TClientDataSet cds = TClientDataSet.create(vs, "CLIFORDEP");
            cds.createDataSet();
            cds.condicao(" WHERE CLIFORDEP.CCLIFOR = " + vs.getParameter("CLIFOR") + " AND CLIFORDEP.CCLIFORDEP = " + vs.getParameter("CLIFORDEP"));
            cds.open();

            JSONObject jasao = new JSONObject();
            jasao.put("NOME", cds.fieldByName("NOME").asString());
            jasao.put("DEP", cds.fieldByName("GRAU").asString());
            jasao.put("DATA", cds.fieldByName("DTNASC").asDate());
            jasao.put("LOCAL", cds.fieldByName("CIDADE").asString());
            jasao.put("UF", cds.fieldByName("UF").asString());
            jasao.put("CPF", cds.fieldByName("CPFDEPENDENTE").asInteger());

            return jasao.toString();
        } catch (ExcecaoTecnicon | JSONException e) {
            throw new ExcecaoTecnicon(vs, "Atenção", e, true);
        }
    }

    public String addDependente(VariavelSessao vs) throws ExcecaoTecnicon {
        Funcoes.validaVSNNNome(vs, new String[]{
            "CCLIFOR", "Código do Cliente"
        }, new String[]{
            "NOME", "Nome do Dependente"
        }, new String[]{
            "GRAU", "Grau de Parentesco"
        }, new String[]{
            "DATA", "Data de Nascimento"
        });

        try {
            String result;
            TClientDataSet cds = TClientDataSet.create(vs, "CLIFORDEP");
            cds.createDataSet();
            cds.condicao(" WHERE CLIFORDEP.NOME = '" + vs.getParameter("NOME") + "' "
                    + " OR (CLIFORDEP.DTNASC = '" + vs.getParameter("DATA") + "' AND CLIFORDEP.CPFDEPENDENTE = " + vs.getParameter("CPF") + ") ");
            cds.open();

            if (cds.isEmpty()) {
                result = "Registro inserido com sucesso!";
                cds.insert();
                cds.fieldByName("CCLIFOR").asInteger(Funcoes.strToInt(vs.getParameter("CCLIFOR")));
            } else {
                result = "Registro atualizado com sucesso!";
                cds.edit();
            }

            cds.fieldByName("NOME").asString(vs.getParameter("NOME"));
            cds.fieldByName("GRAU").asString(vs.getParameter("GRAU"));
            cds.fieldByName("DTNASC").asDate(Funcoes.strToDate(vs, Funcoes.formatarDB(vs.getParameter("DATA"), "D")));
            cds.fieldByName("CIDADE").asString(vs.getParameter("CIDADE"));
            cds.fieldByName("UF").asString(vs.getParameter("UF"));
            cds.fieldByName("CPFDEPENDENTE").asInteger(Funcoes.strToInt(vs.getParameter("CPF")));
            cds.post();

            return result;

        } catch (ExcecaoTecnicon e) {
            throw new ExcecaoMsg(vs, e.getMessage());
        }
    }

    public String editaDep(VariavelSessao vs) throws ExcecaoTecnicon {
        Funcoes.validaVSNNNome(vs, new String[]{
            "CCLIFOR", "Código do Cliente"
        }, new String[]{
            "CCLIFORDEP", "Código do Dependente"
        }, new String[]{
            "NOME", "Nome do Dependente"
        }, new String[]{
            "GRAU", "Grau de Parentesco"
        }, new String[]{
            "DATA", "Data de Nascimento"
        });

        try {
            TClientDataSet cds = TClientDataSet.create("CLIFORDEP");
            cds.createDataSet();
            cds.condicao(" WHERE CLIFORDEP.CCLIFOR = " + vs.getParameter("CCLIFOR") + " AND CLIFORDEP.CCLIFORDEP = " + vs.getParameter("CLIFORDEP") + " ");
            cds.open();
            cds.edit();
            cds.fieldByName("CCLIFOR").asInteger(Funcoes.strToInt(vs.getParameter("CCLIFOR")));
            cds.fieldByName("NOME").asString(vs.getParameter("NOME"));
            cds.fieldByName("GRAU").asInteger(Funcoes.strToInt(vs.getParameter("GRAU")));
            cds.fieldByName("DTNASC").asDate(Funcoes.strToDate(vs, vs.getParameter("DATA")));
            cds.fieldByName("CIDADE").asString(vs.getParameter("CIDADE"));
            cds.fieldByName("UF").asString(vs.getParameter("UF"));
            cds.fieldByName("PLANOSAUDE").asString("N");
            cds.fieldByName("CPFDEPENDENTE").asString(vs.getParameter("CPFDEPENDENTE"));
            cds.post();

            return "Registro atualizado!";
        } catch (Exception e) {
            throw new ExcecaoMsg(vs, e.getMessage());
        }
    }

    public String apagaDep(VariavelSessao vs) throws ExcecaoTecnicon {
        Funcoes.validaVSNNNome(vs, new String[]{
            "CLIFOR", "Código do Cliente"
        }, new String[]{
            "CLIFORDEP", "Código do Dependente"
        });
        try {
            TClientDataSet cds = TClientDataSet.create(vs, "CLIFORDEP");
            cds.createDataSet();
            cds.condicao(" WHERE CLIFORDEP.CCLIFOR = " + vs.getParameter("CLIFOR") + " AND CLIFORDEP.CCLIFORDEP = " + vs.getParameter("CLIFORDEP"));
            cds.open();
            cds.delete();

            return "Registro Excluido com sucesso!";
        } catch (Exception e) {
            throw new ExcecaoMsg(vs, e.getMessage());
        }
    }
}
