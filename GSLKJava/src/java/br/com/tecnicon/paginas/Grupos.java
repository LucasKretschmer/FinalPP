package br.com.tecnicon.paginas;

import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import javax.ejb.Stateless;

/**
 *
 * @author jean.siqueira
 */
@Stateless
public class Grupos{
    
//    private Grupos(){}
    
    public static String obterGrupos(VariavelSessao vs) throws ExcecaoTecnicon
    {
         TClientDataSet cdsGrupo = TClientDataSet.create(vs, "USUARIOGRUPO");
            cdsGrupo.createDataSet();
            cdsGrupo.condicao(" WHERE USUARIOGRUPO.CUSUARIO = " + vs.getValor("cusuario"));
            cdsGrupo.open();

            StringBuilder ids = new StringBuilder();
            int cont = 0;
            ////System.out.println("qtde de grupo:"+cdsGrupo.commandText()+cdsGrupo.condicao());
            while(!cdsGrupo.eof())
            {
                ids.append(cdsGrupo.fieldByName("CGRUSUARIO").asString());
                if (cont < cdsGrupo.recordCount()-1)
                {
                    ids.append(",");
                }
                cont++;
                cdsGrupo.next();
            }
            if (!ids.equals(""))
                return ids.toString();
            else
                throw new ExcecaoTecnicon(vs,"Usuário não esta vinculado a a nenhum grupo!");
    }

//    @Override
//    public String obterTelaHtml(VariavelSessao vs) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
    
}
