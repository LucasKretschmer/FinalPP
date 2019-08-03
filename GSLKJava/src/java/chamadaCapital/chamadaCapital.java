package chamadaCapital;

import br.com.tecnicon.server.context.TClassLoader;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import java.util.Date;
import java.util.Random;
import javax.ejb.Stateless;

/**
 *
 * @author Lucas Kretschmer
 */
@Stateless
public class chamadaCapital {

    public String chamada(VariavelSessao vs) throws ExcecaoTecnicon {
        Funcoes.validaVSNNNome(vs, new String[]{
            "CI", "Cliente Fnicial"
        }, new String[]{
            "CF", "Cliante Final"
        }, new String[]{
            "VALOR", "Valor total"
        }, new String[]{
            "OBS", "OBS"
        });

        double valor = Funcoes.strToDouble(vs.getParameter("VALOR"));

        TClientDataSet cds = TClientDataSet.create(vs, "CLIFOR");
        cds.createDataSet();
        cds.condicao(" WHERE CLIFOR.CCLIFOR BETWEEN " + vs.getParameter("CI") + " AND " + vs.getParameter("CF") + " AND CLIFOR.MALA = 'W' ");
        cds.open();

        cds.first();
        int qtde = cds.fieldDefs().count();
        valor = valor / qtde;
        Random radom = new Random();
        if (!cds.eof()) {
            enviarBoleto(vs, cds.fieldByName("CCLIFOR").asInteger(), valor, "GS-" + radom.nextInt(9999), vs.getParameter("OBS") + "\nPagavel até a data do vencimento!", Funcoes.incMonth(new Date(), 1));

            cds.next();
        }

        return "TOOOOOPPPEEERRRRSOOOONNN";
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
}
