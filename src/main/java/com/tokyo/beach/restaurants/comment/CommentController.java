package com.tokyo.beach.restaurants.comment;

import com.tokyo.beach.restaurants.user.User;
import com.tokyo.beach.restaurants.user.UserDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@CrossOrigin
@RestController
public class CommentController {
    private final CommentRepository commentRepository;
    private CommentDataMapper commentDataMapper;
    private UserDataMapper userDataMapper;

    @Autowired
    public CommentController(CommentRepository commentRepository, CommentDataMapper commentDataMapper, UserDataMapper userDataMapper) {
        this.commentRepository = commentRepository;
        this.commentDataMapper = commentDataMapper;
        this.userDataMapper = userDataMapper;
    }

    @RequestMapping(value = "restaurants/{restaurantId}/comments", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public SerializedComment create(@RequestBody NewComment newComment, @PathVariable String restaurantId) {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = sra.getRequest();
        Number userId = (Number) request.getAttribute("userId");
        Comment persistedComment = commentDataMapper.create(
                newComment,
                userId.longValue(),
                Long.parseLong(restaurantId)
        );
        User currentUser = userDataMapper.get(userId.longValue()).get();
        return new SerializedComment(persistedComment, currentUser);
    }

    @RequestMapping(value = "comments/{commentId}", method = DELETE)
    @ResponseStatus(OK)
    public void delete(@PathVariable String commentId) {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = sra.getRequest();
        Number userId = (Number) request.getAttribute("userId");

        Optional<Comment> maybeCommentToDelete = commentDataMapper.get(Long.parseLong(commentId));

        if (maybeCommentToDelete.isPresent() &&
                userId.longValue() == maybeCommentToDelete.get().getCreatedByUserId()) {
            commentDataMapper.delete(maybeCommentToDelete.get().getId());
        }
    }

    @RequestMapping(value = "restaurants/{restaurantId}/comments", method = GET)
    @ResponseStatus(OK)
    public List<SerializedComment> get(@PathVariable String restaurantId) {
        return commentRepository.findForRestaurant(Long.parseLong(restaurantId));
    }

}
