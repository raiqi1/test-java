package com.example.demo.repository;

import com.example.demo.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    // Find books by title (case-insensitive, partial match)
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    // Find books by author (case-insensitive, partial match)
    List<Book> findByAuthorContainingIgnoreCase(String author);
    
    // Find books by published year
    List<Book> findByPublishedYear(Integer year);
    
    // Find books by ISBN
    List<Book> findByIsbn(String isbn);
    
    // Find books published between years
    List<Book> findByPublishedYearBetween(Integer startYear, Integer endYear);
    
    // Custom query using JPQL
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword%")
    List<Book> findByTitleOrAuthorContaining(@Param("keyword") String keyword);
    
    // Custom native SQL query
    @Query(value = "SELECT * FROM books WHERE published_year > :year ORDER BY published_year DESC", nativeQuery = true)
    List<Book> findBooksPublishedAfterYear(@Param("year") Integer year);
    
    // Count books by author
    Long countByAuthor(String author);
    
    // Check if book exists by ISBN
    boolean existsByIsbn(String isbn);
}