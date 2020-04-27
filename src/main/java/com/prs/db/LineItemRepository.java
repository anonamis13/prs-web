package com.prs.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prs.business.LineItem;
import com.prs.business.Request;

public interface LineItemRepository extends JpaRepository<LineItem, Integer> {

	List<LineItem> findAllByRequest(Optional<Request> request);
	List<LineItem> findAllByRequest(Request request);
}
