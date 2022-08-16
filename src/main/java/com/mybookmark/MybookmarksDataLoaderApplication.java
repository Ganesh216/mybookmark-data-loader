package com.mybookmark;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mybookmark.author.Author;
import com.mybookmark.author.AuthorRepository;
import com.mybookmark.book.Book;
import com.mybookmark.book.BookRepository;
import com.mybookmark.connections.DataStaxAstraProperties;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class MybookmarksDataLoaderApplication {

	@Autowired
	AuthorRepository authorRepository ;
	
	@Autowired
	BookRepository bookRepository ;
	
	@Value("${datadump.location.author}")
	private String authorDumpLocation;
	
	@Value("${datadump.location.books}")
	private String bookDumpLocation;
	
	public static void main(String[] args) {
		SpringApplication.run(MybookmarksDataLoaderApplication.class, args);
	}

	
	@PostConstruct
	public void start() {
		initAuthor();
		initBooks();
	}
	
	private void initBooks() {
		DateTimeFormatter dateFormater = 	DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
		Path path = Paths.get(bookDumpLocation);
		try(Stream<String> lines = Files.lines(path)) {
			lines.forEach(line -> {
				String jsonStr = line.substring(line.indexOf("{"));
				JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject(); 
				
				Book book = new Book();
				
				if(jsonObject.get("key") != null) {
					 String key = jsonObject.get("key").isJsonNull() ? "" : jsonObject.get("key").getAsString();
					 key = key.substring(key.lastIndexOf('/')+1);
					 book.setId(key);
				}
				
				if(jsonObject.get("title") != null) {
					String name = jsonObject.get("title").isJsonNull() ? "" : jsonObject.get("title").getAsString();
					book.setName(name);
				}
				
				if(jsonObject.get("description") != null && jsonObject.get("description").getAsJsonObject().get("value") != null) {
					String description = jsonObject.get("description").getAsJsonObject().get("value").isJsonNull() ? "" : jsonObject.get("description").getAsJsonObject().get("value").getAsString();
					book.setDescription(description);
				}
				
				if(jsonObject.get("created") != null && jsonObject.get("created").getAsJsonObject().get("value") != null) {
					String created = jsonObject.get("created").getAsJsonObject().get("value").isJsonNull() ? "" : jsonObject.get("created").getAsJsonObject().get("value").getAsString();
					book.setPublishedDate(LocalDate.parse(created, dateFormater));
				}
				
				if(jsonObject.get("covers") != null) {
					JsonArray coversArray = jsonObject.get("covers").isJsonNull() ? new JsonArray() : jsonObject.get("covers").getAsJsonArray();
					List<String> coverList = new ArrayList();
					coversArray.forEach(i-> {
						coverList.add(i.getAsString());
					});
					book.setCoverIds(coverList);
				}
				
				if(jsonObject.get("authors") != null) {
					JsonArray authorsArray = jsonObject.get("authors").isJsonNull() ? new JsonArray() : jsonObject.get("authors").getAsJsonArray();
					List<String> authorIds = new ArrayList();
					authorsArray.forEach(i-> {
						String authorId = i.getAsJsonObject().get("author").getAsJsonObject().get("key").getAsString();
						authorId = authorId.substring(authorId.lastIndexOf("/")+1) ;
						authorIds.add(authorId);
					});
					book.setAuthorIds(authorIds);
					
					List<String> authorNames = authorIds.stream().map(id->{
						Optional<Author> author = authorRepository.findById(id);
						if(!author.isPresent()) return "Unknown Author";
						return author.get().getName();
					}).collect(Collectors.toList());
					book.setAuthorNames(authorNames);
				}
				System.out.println("Book Name "+book.getName());
				bookRepository.save(book);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void initAuthor() {
		Path path = Paths.get(authorDumpLocation);
		try(Stream<String> lines = Files.lines(path)){
			lines.forEach(line -> {
				String jsonStr = line.substring(line.indexOf("{"));
				String name = "", personal_name = "", key = "" ;

				JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject(); 
				if(jsonObject.get("name") != null) {
					 name = jsonObject.get("name").isJsonNull() ? "" : jsonObject.get("name").getAsString();
				}
				
				if(jsonObject.get("personal_name") != null) {
					 personal_name = jsonObject.get("personal_name").isJsonNull() ? "" : jsonObject.get("personal_name").getAsString();
				}
				if(jsonObject.get("key") != null) {
					 key = jsonObject.get("key").isJsonNull() ? "" : jsonObject.get("key").getAsString();
					 key = key.substring(key.lastIndexOf('/')+1);
				}
				
				Author author = new Author(key, name, personal_name);
				authorRepository.save(author);
				System.out.println("Author "+author.getName());
			});
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle); 
		
	}
}
