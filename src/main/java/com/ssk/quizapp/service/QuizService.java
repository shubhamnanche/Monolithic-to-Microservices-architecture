package com.ssk.quizapp.service;

import com.ssk.quizapp.dao.QuestionDao;
import com.ssk.quizapp.dao.QuizDao;
import com.ssk.quizapp.model.Question;
import com.ssk.quizapp.model.QuestionWrapper;
import com.ssk.quizapp.model.Quiz;
import com.ssk.quizapp.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuizService {

    @Autowired
    QuizDao quizDao;

    @Autowired
    QuestionDao questionDao;

    public ResponseEntity<String> createQuiz(String category, int numQ, String title) {
        List<Question> questions = questionDao.findRandomQuestionsByCategory(category, numQ);

        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setQuestions(questions);
        quizDao.save(quiz);
        return new ResponseEntity<>("Success", HttpStatus.CREATED);
    }

    public ResponseEntity<List<QuestionWrapper>> getQuizQuestions(int id) {
        Optional<Quiz> quiz = quizDao.findById(id);
        if (quiz.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
        }
        List<Question> questionsFromDB = quiz.get().getQuestions();
        List<QuestionWrapper> questionsForUser = questionsFromDB.stream()
                .map(question ->
                        new QuestionWrapper(
                                question.getId(),
                                question.getCategory(),
                                question.getQuestionTitle(),
                                question.getOption1(),
                                question.getOption2(),
                                question.getOption3(),
                                question.getOption4()
                        )
                ).toList();
        return new ResponseEntity<>(questionsForUser, HttpStatus.OK);
    }

    public ResponseEntity<Integer> calculateResult(Integer id, List<Response> responses) {
        Optional<Quiz> quiz = quizDao.findById(id);
        if (quiz.isEmpty()) {
            return new ResponseEntity<>(0, HttpStatus.NOT_FOUND);
        }
        List<Question> questions = quiz.get().getQuestions();
        int right = 0;
        for (int i = 0; i < responses.size(); i++) {
            int finalI = i;
            Optional<Question> first = questions.stream()
                    .filter(question -> Objects.equals(question.getId(), responses.get(finalI).getId()))
                    .findFirst();
            if (first.isPresent() && responses.get(i).getResponse()
                    .equals(first.get().getRightAnswer())) {
                right++;
            }
        }
        return new ResponseEntity<>(right, HttpStatus.OK);
    }
}
