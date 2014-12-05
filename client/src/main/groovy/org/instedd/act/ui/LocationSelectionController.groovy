package org.instedd.act.ui

import java.awt.event.KeyEvent
import java.util.concurrent.TimeUnit

import javax.swing.JList
import javax.swing.JTextField

import org.instedd.act.Observables
import org.instedd.act.controllers.RegistrationController
import org.instedd.act.models.Location

class LocationSelectionController {

	Location selectedLocation

	RegistrationController parent
	RegistrationForm view
	JTextField input
	JList list

	rx.Observable<String> queries
	rx.Subscription queriesSubscription

	def LocationSelectionController(RegistrationController parent, RegistrationForm view) {
		this.parent = parent
		this.view = view
		this.input = view.locationInput
		this.list = view.locationList

		def keyReleased = Observables.keyReleased(input)

		keyReleased.filter { e -> e.keyCode == KeyEvent.VK_ENTER }
					.subscribe { setCurrentSelection() }

		keyReleased.filter { e -> isNavigationKeyPress(e) && !list.model.empty}
					.subscribe { e -> forwardEventToList(e) }

		Observables.keyPressed(input)
				.filter { e -> isNavigationKeyPress(e)}
				.subscribe { e -> e.consume() }

		Observables.clicks(list)
				.subscribe { setCurrentSelection(); input.requestFocus() }

		queries = Observables.textChange(input)
							.throttleLast(500, TimeUnit.MILLISECONDS)

		subscribeToQueries()
	}

	def forwardEventToList(KeyEvent e) {
		int next = list.selectedIndex

		if (e.keyCode == KeyEvent.VK_DOWN) {
			next++
		} else if (e.keyCode == KeyEvent.VK_UP) {
			next--
			e.keyCode = KeyEvent.VK_DOWN
		}

		next = Math.max(0, next)
		next = Math.min(next, list.model.size - 1)

		synchronized(list.getTreeLock()) {
			list.setSelectedValue(list.model.get(next), true)
		}
	}

	def isNavigationKeyPress(KeyEvent e) {
		e.keyCode == KeyEvent.VK_DOWN || e.keyCode == KeyEvent.VK_UP
	}

	synchronized void clearSelection() {
		selectedLocation = null
	}

	synchronized void setCurrentSelection() {
		if (list.visible && list.selectedValue != null) {
			// temporarily stop listening to new queries, to set input text
			// programatically without triggering a new search
			unsubscribeFromQueries()
			
			def location = list.selectedValue
			input.text = location.pathString
			selectedLocation = location
			
			subscribeToQueries()

			view.hideLocationOptions()
		}
	}

	def subscribeToQueries() {
		queriesSubscription = queries.subscribe { query -> parent.locationInputChanged(query) }
	}

	def unsubscribeFromQueries() {
		queriesSubscription.unsubscribe()
	}
}
