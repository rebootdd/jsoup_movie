package com.douban.movie.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.douban.movie.web.domain.FilmReview;

public interface FilmReviewRepository extends JpaRepository<FilmReview, Long> {

}
