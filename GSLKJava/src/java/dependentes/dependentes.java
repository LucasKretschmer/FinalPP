package dependentes;

import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.dataset.TSQLDataSetEmp;
import br.com.tecnicon.server.execoes.ExcecaoMsg;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import javax.ejb.Stateless;

/**
 *
 * @author lucas.kretschmer
 */
@Stateless
public class dependentes
{

    public String atualizaGrid(VariavelSessao vs) throws ExcecaoTecnicon
    {
        Funcoes.validaVSNNNome(vs, new String[]
        {
            "CCLIFOR", "Código do Cliente"
        });
        try
        {
            int cclifor = Funcoes.strToInt(vs.getParameter("CCLIFOR"));
            int cclifordep;
            String grau;
            String html = "";
            TClientDataSet cdsData = TClientDataSet.create(vs, "CLIFORDEP");
            cdsData.createDataSet();
            cdsData.condicao(" WHERE CLIFORDEP.CCLIFOR = " + cclifor);
            cdsData.open();

            if (!cdsData.isEmpty())
            {
                while (!cdsData.eof())
                {
                    cclifordep = cdsData.fieldByName("CCLIFORDEP").asInteger();
                    switch (cdsData.fieldByName("GRAU").asInteger())
                    {
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
            } else
            {
                html += "<tr>"
                        + "<td></td>"
                        + "<td>Sem dependentes</td>"
                        + "<td></td>"
                        + "<tr>";
            }

            return html;
        } catch (Exception e)
        {
            throw new ExcecaoMsg(vs, e.getMessage());
        }
    }

    public String insertDependente(VariavelSessao vs) throws ExcecaoTecnicon
    {
        Funcoes.validaVSNNNome(vs, new String[]
        {
            "CCLIFOR", "Código do Cliente"
        }, new String[]
        {
            "NOME", "Nome do Dependente"
        }, new String[]
        {
            "GRAU", "Grau de Parentesco"
        }, new String[]
        {
            "DATA", "Data de Nascimento"
        });

        try
        {
            if (!Funcoes.varIsNull(vs.getParameter("CPF")))
            {
                TClientDataSet cds = TClientDataSet.create(vs, "CLIFORDEP");
                cds.createDataSet();
                cds.condicao(" WHERE CLIFORDEP.CPFDEPENDENTE = " + vs.getParameter("CPF") + " AND CLIFORDEP.CCLIFOR = " + vs.getParameter("CCLIFOR"));
                cds.open();

                if (!cds.isEmpty())
                {
                    vs.addParametros("CCLIFORDEP", cds.fieldByName("CCLIFORDEP").asInteger());
                    return updateDep(vs);
                }
            }

            TClientDataSet cds = TClientDataSet.create("CLIFORDEP");
            cds.createDataSet();
            cds.insert();
            cds.fieldByName("CCLIFOR").asInteger(Funcoes.strToInt(vs.getParameter("CCLIFOR")));
            cds.fieldByName("NOME").asString(vs.getParameter("NOME"));
            cds.fieldByName("GRAU").asString(vs.getParameter("GRAU"));
            cds.fieldByName("DTNASC").asDate(Funcoes.strToDate(vs, Funcoes.formatarDB(vs.getParameter("DATA"), "D")));
            cds.fieldByName("CIDADE").asString(vs.getParameter("CIDADE"));
            cds.fieldByName("UF").asString(vs.getParameter("UF"));
            cds.fieldByName("CPFDEPENDENTE").asString(vs.getParameter("CPFDEPENDENTE"));
            cds.post();

            return "Registro inserido com sucesso!";

        } catch (ExcecaoTecnicon e)
        {
            throw new ExcecaoMsg(vs, e.getMessage());
        }
    }

    public String updateDep(VariavelSessao vs) throws ExcecaoTecnicon
    {
        Funcoes.validaVSNNNome(vs, new String[]
        {
            "CCLIFOR", "Código do Cliente"
        }, new String[]
        {
            "CCLIFORDEP", "Código do Dependente"
        }, new String[]
        {
            "NOME", "Nome do Dependente"
        }, new String[]
        {
            "GRAU", "Grau de Parentesco"
        }, new String[]
        {
            "DATA", "Data de Nascimento"
        });

        try
        {
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
        } catch (Exception e)
        {
            throw new ExcecaoMsg(vs, e.getMessage());
        }
    }

    public String deleteDep(VariavelSessao vs) throws ExcecaoTecnicon
    {
        Funcoes.validaVSNNNome(vs, new String[]
        {
            "CCLIFOR", "Código do Cliente"
        }, new String[]
        {
            "CCLIFORDEP", "Código do Dependente"
        });
        try
        {
            TSQLDataSetEmp insert = TSQLDataSetEmp.create(vs);
            insert.execSQL(" DELETE FROM CLIFORDEP "
                    + " WHERE CCLIFOR = " + vs.getParameter("CCLIFOR") + " AND CCLIFORDEP = " + vs.getParameter("CLIFORDEP"));
            return "Registro Excluido com sucesso!";
        } catch (Exception e)
        {
            throw new ExcecaoMsg(vs, e.getMessage());
        }
    }
}
