/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tecnicon.paginas;

import br.com.tecnicon.server.context.TClassLoader;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import javax.ejb.Stateless;

/**
 *
 * @author mauricio.sipmann
 */
@Stateless
public class RetornaDados
{

    public String Sessao(VariavelSessao vs) throws ExcecaoTecnicon
    {
        if (!"".equals(vs.getValor("empresa")))
        {
            TClassLoader.execMethod("TecniconPadroesVS/TecniconPadroesVS", "preenchePadroesVS", vs);
        }

        return vs.getValor("sessao");
    }

}
