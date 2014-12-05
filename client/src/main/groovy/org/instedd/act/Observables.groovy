package org.instedd.act

import java.awt.Component
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter

import javax.swing.JTextField
import javax.swing.event.DocumentListener

class Observables {

	/*
	 * WARNING:
	 * 
	 * All this observers assume the subscriber is still subscribed on the swing listeners.
	 * This means that events generated after an 'unsubscribed' call will still call the
	 * event handlers.
	 * 
	 * This will only happen if the subscription is made directly on the result of this methods.
	 * 
	 * For example: unsubscribing from Observables.textChange(input).take(10) will work because
	 * the observable returned by 'take' does check the state of the subscription. 
	 */
	
	static rx.Observable<String> textChange(JTextField textField) {
		rx.Observable.create({ s ->
			def listener = [
			                insertUpdate: { s.onNext(textField.text) },
			                removeUpdate: { s.onNext(textField.text) },
			                changedUpdate:{ s.onNext(textField.text) },
            ] as DocumentListener
		
			textField.document.addDocumentListener(listener)
		})
	}
	
	static rx.Observable<KeyEvent> keyTyped(JTextField textField) {
		rx.Observable.create({ s ->
			textField.addKeyListener([
				keyTyped: { e -> s.onNext(e) },
				keyPressed: {},
				keyReleased: {}
			] as KeyListener)
		})
	}
	
	static rx.Observable<KeyEvent> keyPressed(JTextField textField) {
		rx.Observable.create({ s ->
			textField.addKeyListener([
				keyTyped: {},
				keyPressed: { e -> s.onNext(e) },
				keyReleased: {}
			] as KeyListener)
		})
	}
	
	static rx.Observable<KeyEvent> keyReleased(JTextField textField) {
		rx.Observable.create({ s ->
			textField.addKeyListener([
				keyTyped: {},
				keyPressed: {},
				keyReleased: { e -> s.onNext(e) }
			] as KeyListener)
		})
	}
	
	static rx.Observable<Void> clicks(Component component) {
		rx.Observable.create({ s ->
			component.addMouseListener([
				mouseClicked: { s.onNext(null) }
			] as MouseAdapter)
		})
	}
	
}
