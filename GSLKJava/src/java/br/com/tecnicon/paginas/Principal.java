package br.com.tecnicon.paginas;

import br.com.tecnicon.server.interfaces.TTela;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.io.Serializable;
import javax.ejb.Stateless;

/**
 * @author jean.siqueira
 */
@Stateless
public class Principal implements TTela, Serializable
{

    @Override
    public String obterTelaHtml(VariavelSessao request)
    {
        String propriedade = request.getParameter("cabpropriedadesuino");
        String npropriedade = request.getParameter("abpropriedadesuino");

        if (propriedade != null && !propriedade.equals("cabpropriedadesuino"))
        {
            request.addParametros("cpropriedadesuino", propriedade);
            request.addParametros("propriedadesuino", npropriedade);
        }

        return "";
    }

}
