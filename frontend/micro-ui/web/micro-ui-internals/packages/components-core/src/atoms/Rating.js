import React, { useRef } from "react";
import PropTypes from "prop-types";
import { SVG } from "./SVG";

const Rating = (props) => {
  var stars = [];
  const star = useRef(null);

  for (var i = 1; i <= props.maxRating; i++) {
    if (i - props.currentRating <= 0) {
      const index = i;
      // stars.push(<img key={i} src={starfilled} className="rating-star" alt="star filled" ref={star} onClick={(e,ref)=>props.onFeedback(e,ref)}/>)
      stars.push(
        <SVG.StarFilled
          key={i}
          id={`${props.id}gradient${i}`}
          className="digit-rating-star"
          styles={props.starStyles}
          onClick={(e) => props.onFeedback(e, star, index)}
        />
      );
    } else if (i - props.currentRating > 0 && i - props.currentRating < 1) {
      const index = i;
      stars.push(
        <SVG.StarEmpty
          key={i}
          id={`${props.id}gradient${i}`}
          className="digit-rating-star"
          styles={props.starStyles}
          onClick={(e) => props.onFeedback(e, star, index)}
          percentage={Math.round((props.currentRating - parseInt(props.currentRating)) * 100)}
        />
      );
    } else {
      const index = i;
      // stars.push(<img key={i} src={starempty} className="rating-star" alt="star empty" ref={star} onClick={(e,ref)=>props.onFeedback(e,ref)}/>)
      stars.push(
        <SVG.StarEmpty
          key={i}
          className={`digit-rating-star ${props?.className ? props?.className : ""}`}
          styles={props?.starStyles}
          onClick={(e) => props.onFeedback(e, star, index)}
        />
      );
    }
  }

  return (
    <div className={`${props?.withText ? "digit-rating-with-text" : "digit-rating-star-wrap"}`} style={{ ...props?.styles }}>
      {props.text ? props.text : ""} {stars}
    </div>
  );
};

Rating.propTypes = {
  maxRating: PropTypes.number,
  currentRating: PropTypes.number,
  onFeedback: PropTypes.func,
  id: PropTypes.string,
  withText: PropTypes.bool,
  styles: PropTypes.object,
  className: PropTypes.string,
  text: PropTypes.string,
  starStyles: PropTypes.object,
};

Rating.defaultProps = {
  maxRating: 5,
  currentRating: 0,
  onFeedback: () => {},
  id: "0",
  withText: false,
  styles: {},
  className: "",
  text: "",
  starStyles: {},
};

export default Rating;
