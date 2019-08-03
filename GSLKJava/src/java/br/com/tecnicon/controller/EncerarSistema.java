package br.com.tecnicon.controller;

import br.com.tecnicon.police.TValidaSessao;
import br.com.tecnicon.server.sessao.VariavelSessao;
import javax.ejb.Stateless;

/**
 *
 * @author jean.siqueira
 */
@Stateless
public class EncerarSistema
{

    public String obterTelaHtml(VariavelSessao vs)
    {
        return encerraSessao(vs, "Usuário saiu do sistema");
    }
    
    public String encerraSessao(VariavelSessao vs, String motivo)
    {
        try
        {
            TValidaSessao.eliminarSessao(vs, motivo);
            vs.addParametros("sessao", ""); //TODO - Sessao encerra e hash da vs fica em memória
            return "OK";
        } catch (Exception ex)
        {
            return "erro:" + ex.getMessage(); 
        }
    }
}
