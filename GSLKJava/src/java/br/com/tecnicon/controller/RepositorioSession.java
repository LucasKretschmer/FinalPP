package br.com.tecnicon.controller;

import br.com.tecnicon.police.SingletonGenerico;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author jean.siqueira
 */
public class RepositorioSession
{

    private static final RepositorioSession INSTANCE = new RepositorioSession();

    private RepositorioSession()
    {
        SingletonGenerico.getInstance().setaObjeto("REPO_SESSION_CONTROLLER", new ConcurrentHashMap<String, VariavelSessao>());
    }

    public static RepositorioSession getInstance()
    {
        return INSTANCE;
    }

    public VariavelSessao getAtribute(String sessao) throws ExcecaoTecnicon
    {
        return ((ConcurrentHashMap<String, VariavelSessao>) 
                SingletonGenerico.getInstance().retornaObjeto("REPO_SESSION_CONTROLLER")).get(sessao);
    }

    public void setAttribute(String sessao, VariavelSessao vs)
    {
        ((ConcurrentHashMap<String, VariavelSessao>) 
                SingletonGenerico.getInstance().retornaObjeto("REPO_SESSION_CONTROLLER")).put(sessao, vs);
    }

    public void removeAttribute(String sessao)
    {
        ConcurrentHashMap<String, VariavelSessao> c = ((ConcurrentHashMap<String, VariavelSessao>) 
                SingletonGenerico.getInstance().retornaObjeto("REPO_SESSION_CONTROLLER"));
        
        if (c.containsKey(sessao))
        {
            c.remove(sessao);
        }
    }

    public ConcurrentHashMap<String, VariavelSessao> getSess()
    {
        return (ConcurrentHashMap<String, VariavelSessao>) 
                SingletonGenerico.getInstance().retornaObjeto("REPO_SESSION_CONTROLLER");
    }
}
