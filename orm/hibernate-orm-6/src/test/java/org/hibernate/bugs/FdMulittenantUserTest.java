package org.hibernate.bugs;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import jakarta.persistence.Table;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.PartitionKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using the Java Persistence API.
 */
public class FdMulittenantUserTest {

	private EntityManagerFactory entityManagerFactory;

	@Before
	public void init() {
		entityManagerFactory = Persistence.createEntityManagerFactory( "templatePU" );
	}

	@After
	public void destroy() {
		entityManagerFactory.close();
	}

	// Entities are auto-discovered, so just add them anywhere on class-path
	// Add your tests, using standard JUnit.
	@Test
	public void HHH16849() throws Exception {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		// Do stuff...

		doInJPA( ()->entityManagerFactory, em -> {
			FdMultitenantUser fdMultitenantUser = em.find(FdMultitenantUser.class, 1);
			System.out.println(fdMultitenantUser.getId());

			/*Query query = em.createQuery("SELECT e FROM FdMultitenantUser e WHERE e.id in :ids");
			List<Long> ids = new LinkedList<>();
			ids.add(1L);
			query.setParameter("ids", ids);

			List<FdMultitenantUser> resultList = query.getResultList();
			System.out.println("result size: " + resultList.size());*/
		});

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	@Entity
	@Table(name = "fd_multitenant_users")
	public class FdMultitenantUser
		implements Serializable {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "id")
		private Long id;
		@Column(name = "account_id")
		private Long accountId;
		@Column(name = "name")
		private String name;
		@OneToOne
		@Fetch(FetchMode.JOIN)
		@JoinColumn(name = "id", referencedColumnName = "id", updatable = false, insertable = false)
		private UserProfile userProfile;
		@OneToOne
		@Fetch(FetchMode.JOIN)
		@JoinColumn(name = "id", referencedColumnName = "userId", updatable = false, insertable = false)
		private FdMultitenantUserEmail userEmail;
		@OneToOne
		@Fetch(FetchMode.JOIN)
		@JoinColumn(name = "id", referencedColumnName = "userId", updatable = false, insertable = false)
		private FdMultitenantAuthorization authorization;

		public Long getId(){
			return id;
		}
	}

	@Entity
	@Table(name = "user_profiles")
	public class UserProfile
		implements Serializable {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "id")
		private Long id;
	}

	@Entity
	@Table(name = "fd_multitenant_user_emails")
	public class FdMultitenantUserEmail {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "id")
		private Long id;

		@Column(name = "user_id")
		private Long userId;
	}

	@Entity
	@Table(name = "fd_multitenant_authorizations")
	public class FdMultitenantAuthorization {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "id")
		private Long id;

		@Column(name = "user_id")
		private Long userId;
	}

}