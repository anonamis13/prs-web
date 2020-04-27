package com.prs.web;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import com.prs.business.JsonResponse;
import com.prs.business.LineItem;
import com.prs.business.Product;
import com.prs.business.Request;
import com.prs.db.LineItemRepository;
import com.prs.db.RequestRepository;

@RestController
@RequestMapping("/line-items")
public class LineItemController {
	
	@Autowired
	private LineItemRepository lineItemRepo;
	
	@Autowired
	private RequestRepository requestRepo;

	@GetMapping("/")
	public JsonResponse list() {
		JsonResponse jr = null;
		List<LineItem> lineItems = lineItemRepo.findAll();
		if (lineItems.size() > 0) {
			jr = JsonResponse.getInstance(lineItems);
		} else {
			jr = JsonResponse.getErrorInstance("No lineItems found.");
		}
		return jr;
	}

	@GetMapping("/{id}")
	public JsonResponse get(@PathVariable int id) {
		JsonResponse jr = null;
		Optional<LineItem> lineItem = lineItemRepo.findById(id);
		if (lineItem.isPresent()) {
			jr = JsonResponse.getInstance(lineItem.get());
		} else {
			jr = JsonResponse.getErrorInstance("No lineItems found for id: " + id);
		}
		return jr;
	}
	
	@GetMapping("/lines-for-pr/{id}")
	public JsonResponse getLineItemsForRequest(@PathVariable int id) {
		JsonResponse jr = null;
		Optional<Request> requests = requestRepo.findById(id);
		List<LineItem> lineItems = lineItemRepo.findAllByRequest(requests);

		if (!lineItems.isEmpty()) {
			jr = JsonResponse.getInstance(lineItems);
		} else {
			jr = JsonResponse.getErrorInstance("No lineItems found for request id: " + id);
		}
		return jr;
	}

	@PostMapping("/")
	public JsonResponse createLineItem(@RequestBody LineItem l) {
		JsonResponse jr = null;

		try {
			l = lineItemRepo.save(l);
			Request request = l.getRequestId();
			request.setTotal(recalculateTotal(request));
			requestRepo.save(request);
			jr = JsonResponse.getInstance(l);
		} catch (DataIntegrityViolationException dive) {
			jr = JsonResponse.getErrorInstance(dive.getRootCause().getMessage());
			dive.printStackTrace();
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error creating lineItem: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;
	}

	@PutMapping("/")
	public JsonResponse updateLineItem(@RequestBody LineItem l) {
		JsonResponse jr = null;

		try {
			l = lineItemRepo.save(l);
			jr = JsonResponse.getInstance(l);
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error updating lineItem: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;
	}

	@DeleteMapping("/{id}")
	public JsonResponse deleteLineItem(@PathVariable int id) {
		JsonResponse jr = null;

		try {
			lineItemRepo.deleteById(id);
			jr = JsonResponse.getInstance("LineItem id: " + id + " deleted successfully.");
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error deleting lineItem: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;
	}
	
	public double recalculateTotal(Request r) {
		
		List<LineItem> lineItems = lineItemRepo.findAllByRequest(r);
		double total = 0.0;
		
		for (LineItem lineItem: lineItems) {
			Product product = lineItem.getProductId();
			total += product.getPrice() * lineItem.getQuantity();
		}
		
		return total;
	}
}
