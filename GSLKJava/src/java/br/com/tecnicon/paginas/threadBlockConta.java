/*
 * Classe Runnable que controla as contas bloqueadas por erro de usuario e senha em exceco
 */
package br.com.tecnicon.paginas;

import br.com.tecnicon.police.ListaUsuarios;
import br.com.tecnicon.server.util.funcoes.Funcoes;

/**
 *
 * @author mauricio.sipmann
 */
public class threadBlockConta implements Runnable
{

    private ListaUsuarios listaUs;
    private int cuser;

    public threadBlockConta(ListaUsuarios listaUs, int cuser)
    {
        this.listaUs = listaUs;
        this.cuser = cuser;
    }

    @Override
    public void run()
    {
        listaUs.getUsuarioBloqueados().put(cuser, true);
        Funcoes.tSleep(listaUs.getHorasBloc() * 60 * 60 * 1000);
        listaUs.getUsuarioBloqueados().put(cuser, false);
        listaUs.getUsuarioBloqueados().remove(cuser);
    }

}
