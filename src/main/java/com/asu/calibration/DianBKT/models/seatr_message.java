package com.asu.calibration.DianBKT.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table
@Entity
public class seatr_message implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8970977115131474109L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	@Column(name = "student_id")
	private int student_id;
	@Column(name = "question_id")
	private int question_id;
	@Column(name = "format_id")
	private int format_id;
	@Column(name = "correct")
	private int correct;
	@Column(name = "timestamp")
	private String timestamp;
	
	public seatr_message() {
    }
    public seatr_message(int id,int student_id, int question_id,int format_id, int  correct, String timestamp ) {
        this.id = id;
        this.student_id = student_id;
        this.question_id = question_id;
        this.format_id = format_id;
        this.correct = correct;
        this.timestamp = timestamp;
    }
    
    public seatr_message(int student_id, int question_id,int format_id, int  correct, String timestamp ) {
        this.student_id = student_id;
        this.question_id = question_id;
        this.format_id = format_id;
        this.correct = correct;
        this.timestamp = timestamp;
    }
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStudent_id() {
		return student_id;
	}
	public void setStudent_id(int student_id) {
		this.student_id = student_id;
	}
	public int getQuestion_id() {
		return question_id;
	}
	public void setQuestion_id(int question_id) {
		this.question_id = question_id;
	}
	public int getFormat_id() {
		return format_id;
	}
	public void setFormat_id(int format_id) {
		this.format_id = format_id;
	}
	public int getCorrect() {
		return correct;
	}
	public void setCorrect(int correct) {
		this.correct = correct;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	
}
