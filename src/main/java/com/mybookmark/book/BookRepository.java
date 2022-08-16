/**
 * 
 */
package com.mybookmark.book;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Ganesh
 *
 */

@Repository
public interface BookRepository extends CassandraRepository<Book, String>{

	
	
}
