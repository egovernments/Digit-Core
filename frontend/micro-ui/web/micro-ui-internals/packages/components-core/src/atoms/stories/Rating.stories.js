import React from "react";
import Rating from "../Rating";

export default {
  title: "Atoms/Rating",
  component: Rating,
  argTypes: {
    maxRating: {
      control: { type: "number", min: 1 },
    },
    currentRating: {
      control: { type: "number", min: 0, max: 5 },
    },
    onFeedback: {
      action: "Rating changed",
    },
    id: {
      control: "text",
    },
    withText: {
      control: "boolean",
    },
    styles: {
      control: { type: "object" },
    },
    className: {
      control: "text",
    },
    text: {
      control: "text",
    },
    starStyles: {
      control: { type: "object" },
    },
  },
};

const Template = (args) => <Rating {...args} />;

export const Default = Template.bind({});
Default.args = {
  maxRating: 5,
  currentRating: 3,
  onFeedback: (index) => console.log(`Rating changed to ${index}`),
};

export const WithText = Template.bind({});
WithText.args = {
  maxRating: 5,
  currentRating: 4,
  onFeedback: (index) => console.log(`Rating changed to ${index}`),
  withText: true,
  text: "Rate this product:",
};

export const WithCustomStyles = Template.bind({});
WithCustomStyles.args = {
  maxRating: 5,
  currentRating: 2.5,
  onFeedback: (index) => console.log(`Rating changed to ${index}`),
  styles: {
    fontSize: "18px",
    color: "#333",
    fontWeight: "bold",
    margin: "10px 0",
  },
  starStyles: {
    color: "#FFD700",
  },
};
