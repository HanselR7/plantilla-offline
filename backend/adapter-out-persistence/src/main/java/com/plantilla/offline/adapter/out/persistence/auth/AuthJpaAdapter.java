package com.plantilla.offline.adapter.out.persistence.auth;

import com.plantilla.offline.application.auth.AuthRepository;
import com.plantilla.offline.application.auth.AuthUser;
import com.plantilla.offline.application.auth.StoredUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@Transactional
public class AuthJpaAdapter implements AuthRepository {

    @PersistenceContext(unitName = "plantillaPU")
    private EntityManager em;

    @Override
    public long countUsers() {
        return ((Number) em.createNativeQuery("select count(*) from app_user").getSingleResult()).longValue();
    }

    @Override
    public Optional<StoredUser> findUser(String username) {
        List<?> rows = em.createNativeQuery(
                        "select id, username, password_hash, role, disabled_at from app_user "
                                + "where lower(username) = lower(:u)")
                .setParameter("u", username)
                .getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] r = (Object[]) rows.get(0);
        return Optional.of(new StoredUser((UUID) r[0], (String) r[1], (String) r[2], (String) r[3], r[4] != null));
    }

    @Override
    public void createUser(UUID id, String username, String passwordHash, String role) {
        em.createNativeQuery("insert into app_user (id, username, password_hash, role) values (:id, :u, :p, :r)")
                .setParameter("id", id)
                .setParameter("u", username)
                .setParameter("p", passwordHash)
                .setParameter("r", role)
                .executeUpdate();
    }

    @Override
    public void createSession(UUID id, UUID userId, String accessHash, Instant accessExpiresAt,
                              String refreshHash, Instant refreshExpiresAt) {
        em.createNativeQuery(
                        "insert into auth_session (id, user_id, access_hash, access_expires_at, "
                                + "refresh_hash, refresh_expires_at) values (:id, :uid, :ah, :ae, :rh, :re)")
                .setParameter("id", id)
                .setParameter("uid", userId)
                .setParameter("ah", accessHash)
                .setParameter("ae", accessExpiresAt)
                .setParameter("rh", refreshHash)
                .setParameter("re", refreshExpiresAt)
                .executeUpdate();
    }

    @Override
    public Optional<AuthUser> findUserByAccessHash(String accessHash, Instant now) {
        List<?> rows = em.createNativeQuery(
                        "select u.id, u.username, u.role from auth_session s join app_user u on u.id = s.user_id "
                                + "where s.access_hash = :h and s.revoked_at is null "
                                + "and s.access_expires_at > :now and u.disabled_at is null")
                .setParameter("h", accessHash)
                .setParameter("now", now)
                .getResultList();
        return rows.isEmpty() ? Optional.empty() : Optional.of(toUser((Object[]) rows.get(0)));
    }

    @Override
    public Optional<AuthUser> rotateSession(String oldRefreshHash, Instant now, String newAccessHash,
                                            Instant newAccessExpiresAt, String newRefreshHash,
                                            Instant newRefreshExpiresAt) {
        // Un solo UPDATE condicional: un refresh token solo se puede canjear una vez.
        int rows = em.createNativeQuery(
                        "update auth_session set access_hash = :na, access_expires_at = :nae, "
                                + "refresh_hash = :nr, refresh_expires_at = :nre "
                                + "where refresh_hash = :old and revoked_at is null and refresh_expires_at > :now")
                .setParameter("na", newAccessHash)
                .setParameter("nae", newAccessExpiresAt)
                .setParameter("nr", newRefreshHash)
                .setParameter("nre", newRefreshExpiresAt)
                .setParameter("old", oldRefreshHash)
                .setParameter("now", now)
                .executeUpdate();
        if (rows != 1) {
            return Optional.empty();
        }
        List<?> users = em.createNativeQuery(
                        "select u.id, u.username, u.role from auth_session s join app_user u on u.id = s.user_id "
                                + "where s.refresh_hash = :nr and u.disabled_at is null")
                .setParameter("nr", newRefreshHash)
                .getResultList();
        return users.isEmpty() ? Optional.empty() : Optional.of(toUser((Object[]) users.get(0)));
    }

    @Override
    public void revokeByAccessHash(String accessHash) {
        em.createNativeQuery("update auth_session set revoked_at = now() where access_hash = :h and revoked_at is null")
                .setParameter("h", accessHash)
                .executeUpdate();
    }

    private AuthUser toUser(Object[] r) {
        return new AuthUser((UUID) r[0], (String) r[1], (String) r[2]);
    }
}