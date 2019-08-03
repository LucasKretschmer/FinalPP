package br.com.tecnicon.controller;

import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.ejb.Stateless;

@Stateless
public class SessionControl
{
    public VariavelSessao obterVariavelSessao(String idSessao) throws ExcecaoTecnicon
    {
        return RepositorioSession.getInstance().getAtribute(idSessao);
    }

    public ConcurrentHashMap<String, VariavelSessao> getSess()
    {
        return RepositorioSession.getInstance().getSess();
    }
    
    public String removeSession(String idSessao)
    {
        RepositorioSession.getInstance().removeAttribute(idSessao);
        return "OK";
    }
}