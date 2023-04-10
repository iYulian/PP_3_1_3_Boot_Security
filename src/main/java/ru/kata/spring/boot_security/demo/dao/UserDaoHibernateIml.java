package ru.kata.spring.boot_security.demo.dao;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


@Repository
public class UserDaoHibernateIml implements UserDaoHibernate {

    @PersistenceContext
    private EntityManager entityManager;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    private final String userRole = "ROLE_USER";
    private final String adminRole = "ROLE_ADMIN";

    @Override
    public void saveUser(User user, String authority1, String authority2) {

        AtomicBoolean a = new AtomicBoolean(true);
        AtomicBoolean b = new AtomicBoolean(true);

        if (user.getId() != 0) {
            getUserById(user.getId()).getAuthorities().forEach(x -> {

                if (x.getAuthority().equals(userRole)) {
                    a.set(false);
                }
                if (x.getAuthority().equals(adminRole)) {
                    b.set(false);
                }
            });
        }

        if (!(a.get() & authority1.isEmpty())) {
            user.userAddAuthority(authority1);
        }

        if (!(b.get() & authority2.isEmpty())) {
            user.userAddAuthority(authority2);
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getUserPassword()));

        if (user.getId() == 0) {
            entityManager.persist(user);
        } else {
            entityManager.merge(user);
        }

    }

    @Override
    public void removeUserById(long id) {
        User user = entityManager.find(User.class, id);
        entityManager.remove(user);

    }

    @Override
    public List<User> getAllUsers() {

        List<User> userList = entityManager.createQuery("from User", User.class).getResultList();
        return userList;

    }

    @Override
    public User getUserById(long id) {
        return entityManager.find(User.class, id);
    }

    @Override
    public User getFirstUserByName(String name) {
        List<User> userList = entityManager.
                createQuery("from User where name = '" + name + "'", User.class).getResultList();
        if (userList.isEmpty()){
            return null;
        }
        return  userList.get(0);
    }
}
