package com.ssk.quiz_service.service;

import com.ssk.quiz_service.dao.QuizDao;
import com.ssk.quiz_service.feign.QuizInterface;
import com.ssk.quiz_service.model.QuestionWrapper;
import com.ssk.quiz_service.model.Quiz;
import com.ssk.quiz_service.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class QuizService {

    @Autowired
    QuizDao quizDao;

    @Autowired
    QuizInterface quizInterface;

    public ResponseEntity<String> createQuiz(String category, int numQ, String title) {
        List<Integer> questionIds = quizInterface.getQuestionsForQuiz(category, numQ).getBody();

        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setQuestionIds(questionIds);
        quizDao.save(quiz);
        return new ResponseEntity<>("Success", HttpStatus.CREATED);
    }

    public ResponseEntity<List<QuestionWrapper>> getQuizQuestions(int id) {
        Optional<Quiz> quiz = quizDao.findById(id);
        if (quiz.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
        }
        List<Integer> questionIdsFromDB = quiz.get().getQuestionIds();
        List<QuestionWrapper> questionsForUser = quizInterface.getQuestionsFromIds(questionIdsFromDB).getBody();
        return new ResponseEntity<>(questionsForUser, HttpStatus.OK);
    }

    public ResponseEntity<Integer> calculateResult(Integer id, List<Response> responses) {
        Optional<Quiz> quiz = quizDao.findById(id);
        if (quiz.isEmpty()) {
            return new ResponseEntity<>(0, HttpStatus.NOT_FOUND);
        }
        Integer right = quizInterface.getScore(responses).getBody();
        return new ResponseEntity<>(right, HttpStatus.OK);
    }
}
