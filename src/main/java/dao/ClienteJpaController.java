/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dao.exceptions.NonexistentEntityException;
import dto.Cliente;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import util.AESUtil;
import util.SHA512Util;

/**
 *
 * @author harol
 */
public class ClienteJpaController implements Serializable {

    public ClienteJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_ExamenPrac01_war_1.0-SNAPSHOTPU");

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public ClienteJpaController() {
    }

    public void create(Cliente cliente) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(cliente);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Cliente cliente) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            cliente = em.merge(cliente);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = cliente.getCodiClie();
                if (findCliente(id) == null) {
                    throw new NonexistentEntityException("The cliente with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Cliente cliente;
            try {
                cliente = em.getReference(Cliente.class, id);
                cliente.getCodiClie();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The cliente with id " + id + " no longer exists.", enfe);
            }
            em.remove(cliente);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Cliente> findClienteEntities() {
        return findClienteEntities(true, -1, -1);
    }

    public List<Cliente> findClienteEntities(int maxResults, int firstResult) {
        return findClienteEntities(false, maxResults, firstResult);
    }

    private List<Cliente> findClienteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Cliente.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Cliente findCliente(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Cliente.class, id);
        } finally {
            em.close();
        }
    }

    public Cliente findClienteByDni(String dni) {
        EntityManager em = getEntityManager(); // Obtiene un EntityManager
        try {
            // Crea una consulta JPQL para buscar un Cliente por su ndniClie
            TypedQuery<Cliente> query = em.createQuery(
                    "SELECT c FROM Cliente c WHERE c.ndniClie = :dni", Cliente.class);
            query.setParameter("dni", dni);
            // setMaxResults(1) es una optimización si solo te interesa saber si existe al menos uno.
            // Si esperas estrictamente uno o ninguno, getSingleResult() es apropiado.
            return query.getSingleResult(); // Lanza NoResultException si no se encuentra
        } catch (NoResultException e) {
            return null; // Devuelve null si no se encuentra ningún cliente con ese DNI
        } finally {
            if (em != null) {
                em.close(); // Cierra el EntityManager
            }
        }
    }

    public Cliente findClienteByLogin(String login) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Cliente> query = em.createQuery(
                    "SELECT c FROM Cliente c WHERE c.logiClie = :login", Cliente.class);
            query.setParameter("login", login);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null; // Devuelve null si no se encuentra ningún cliente con ese login
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public Cliente validarUsuario(Cliente u) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createNamedQuery("Cliente.validar", Cliente.class);
            q.setParameter("logiClie", u.getLogiClie());
            q.setParameter("passClie", u.getPassClie());
            return (Cliente) q.getSingleResult();
        } catch (Exception ex) {
            String mensaje = ex.getMessage();
            return null;
        } finally {
            em.close();
        }
    }

    public int getClienteCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Cliente> rt = cq.from(Cliente.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public int calcularEdad(Date fecha) {
        Calendar fechaInicial = Calendar.getInstance();
        fechaInicial.setTime(fecha);
        Calendar fechaActual = Calendar.getInstance();
        int edad = fechaActual.get(Calendar.YEAR) - fechaInicial.get(Calendar.YEAR);
        if (fechaActual.get(Calendar.DAY_OF_YEAR) < fechaInicial.get(Calendar.DAY_OF_YEAR)) {
            edad--;
        }
        return edad;
    }

    public String cambiarClave(Cliente u, String nuevaClave) {
        EntityManager em = getEntityManager();
        try {
            Cliente usuario = validarUsuario(u);
            if (usuario != null) { // Verifica que el usuario exista
                if (usuario.getPassClie().equals(u.getPassClie())) {
                    usuario.setPassClie(nuevaClave);
                    edit(usuario);
                    return "Clave cambiada";
                } else {
                    return "Clave actual no válida";
                }
            } else {
                return "Usuario no encontrado"; // Manejo de usuario no encontrado
            }
        } catch (Exception ex) {
            return null;
        } finally {
            em.close();
        }
    }

    public static void main(String[] args) throws Exception {
        ClienteJpaController vurDAO = new ClienteJpaController();
        Cliente vur = vurDAO.validarUsuario(new Cliente("harol", "3c9909afec25354d551dae21590bb26e38d53f2173b8d"
                + "3dc3eee4c047e7ab1c1eb8b85103e3be7ba613b31bb5c9c36214dc9f14a42fd7a2fdb84856bca5c44c2"));
        String clave = "1234567890123456";
        String pass = "123";
        String contraCifrada = AESUtil.cifrar(pass, clave);
        System.out.println("Contra cifrada: " + contraCifrada);
        System.out.println("---------------------------");
        String contraDescifrada = AESUtil.descifrar(contraCifrada, clave);
        System.out.println("Contra descifrada: " + contraDescifrada);
        System.out.println("---------------------------");
        String contraSHA512 = SHA512Util.hash(contraDescifrada);
        System.out.println("Contra usando SHA512: " + contraSHA512);

        if (vur != null) {
            System.out.println("PERSONA ENCONTRADA");
        } else {
            System.out.println("PERSONA NO ENCONTRADA");
        }
    }

}
