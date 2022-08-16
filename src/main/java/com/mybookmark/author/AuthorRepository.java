/**
 * 
 */
package com.mybookmark.author;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Ganesh
 *
 */

@Repository
public interface AuthorRepository extends CassandraRepository<Author, String>{

	
	
}
