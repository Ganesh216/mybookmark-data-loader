/**
 * 
 */
package com.mybookmark.author;

import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * @author Ganesh
 *
 */

@Table(value = "author")
public class Author {

	@Id @PrimaryKeyColumn(name = "author_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String id;
	
	@Column("author_name")
	@CassandraType(type = Name.TEXT)
	private String name ;
	
	@Column("personal_name")
	@CassandraType(type = Name.TEXT)
	private String personalName ;

	
	/**
	 * @param id
	 * @param name
	 * @param personalName
	 */
	public Author(String id, String name, String personalName) {
		super();
		this.id = id;
		this.name = name;
		this.personalName = personalName;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the personalName
	 */
	public String getPersonalName() {
		return personalName;
	}

	/**
	 * @param personalName the personalName to set
	 */
	public void setPersonalName(String personalName) {
		this.personalName = personalName;
	}
}
