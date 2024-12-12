package com.ssk.question_service.service;

import com.ssk.question_service.dao.QuestionDao;
import com.ssk.question_service.model.Question;
import com.ssk.question_service.model.QuestionWrapper;
import com.ssk.question_service.model.Response;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

@Service
public class QuestionService {

    @Autowired
    QuestionDao questionDao;

    public ResponseEntity<List<Question>> getAllQuestions() {
        try {
            return new ResponseEntity<>(questionDao.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<List<Question>> getQuestionsByCategory(String category) {
        try {
            return new ResponseEntity<>(questionDao.findByCategory(category), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<String> addQuestion(Question question) {
        questionDao.save(question);
        return new ResponseEntity<>("success", HttpStatus.CREATED);
    }

    public ResponseEntity<List<Integer>> getQuestionsForQuiz(String category, Integer numQ) {
        List<Integer> questions = questionDao.findRandomQuestionsByCategory(category, numQ);
        return new ResponseEntity<>(questions, HttpStatus.OK);
    }

    public ResponseEntity<List<QuestionWrapper>> getQuestionsFromIds(@RequestBody List<Integer> questionIds) {
        List<QuestionWrapper> wrappers = questionIds.stream().map(id -> {
                    Optional<Question> question = questionDao.findById(id);
                    return question.map(value -> new QuestionWrapper(
                                    value.getId(),
                                    value.getCategory(),
                                    value.getQuestionTitle(),
                                    value.getOption1(),
                                    value.getOption2(),
                                    value.getOption3(),
                                    value.getOption4()
                            )
                    ).orElse(null);
                }
        ).toList();

        return new ResponseEntity<>(wrappers, HttpStatus.OK);
    }


    public ResponseEntity<Integer> getScore(List<Response> responses) {
        int score = responses.stream()
                .map(response -> {
                            Optional<Question> question = questionDao.findById(response.getId());
                            return question.map(value ->
                                    value.getRightAnswer().equals(response.getResponse()) ? 1 : 0
                            ).orElse(0);
                        }
                )
                .reduce(Integer::sum).get();

        return new ResponseEntity<>(score, HttpStatus.OK);
    }


}
