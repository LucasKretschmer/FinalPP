/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tecnicon.controller;

import br.com.tecnicon.police.ListaSessoes;
import br.com.tecnicon.police.TValidaSessao;
import br.com.tecnicon.server.bd.entity.Sessao;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.util.Date;
import javax.servlet.http.*;

public class THttpSessionListener implements HttpSessionListener
{

    private static ListaSessoes listaSessoes = ListaSessoes.getInstance();

    @Override
    public void sessionCreated(HttpSessionEvent se)
    {
        /*HttpSession session = se.getSession();
         System.out.print(getTime() + " (session) Created:");
         System.out.println("ID=" + session.getId() + " MaxInactiveInterval="
         + session.getMaxInactiveInterval());*/
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se)
    {
        try
        {
            verificaRenovarSessao(se);
        } catch (ExcecaoTecnicon ex)
        {
            
        }
    }

    private String getTime()
    {
        return new Date(System.currentTimeMillis()).toString();
    }

    private void verificaRenovarSessao(HttpSessionEvent se) throws ExcecaoTecnicon
    {
        /* NÃO USADO
        VariavelSessao vsTemp = new TVariavelSessao();
        try
        {
            HttpSession session = se.getSession();
            if (session.getAttribute("vs") != null && !((VariavelSessao) session.getAttribute("vs")).getValor("sessao").equals(""))
            {

                long s = Integer.parseInt(((VariavelSessao) session.getAttribute("vs")).getValor("sessao"));
                if (ListaSessoes.sessoesAtivas.containsKey(s))
                {
                    Sessao sessao = ListaSessoes.sessoesAtivas.get(s);
                    if (TValidaSessao.tempoSessaoExpirou(sessao))
                    {
                        vsTemp.addParametros("nome", sessao.nome);
                        vsTemp.addParametros("empresa", sessao.empresa);
                        vsTemp.addParametros("sessao", "" + sessao.ssessao);

                        TValidaSessao.eliminarSessao(vsTemp, "TIMEOUT SERVLET");
                    }
                    //sessionCreated(se);
                }

            }
        } catch (Exception ex)
        {
            throw new ExcecaoTecnicon(vsTemp, ex);
        }
        */
    }
}
