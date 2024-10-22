import React from "react";
import { SentimentVeryDissatisfied } from "./SentimentVeryDissatisfied";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SentimentVeryDissatisfied",
  component: SentimentVeryDissatisfied,
};

export const Default = () => <SentimentVeryDissatisfied />;
export const Fill = () => <SentimentVeryDissatisfied fill="blue" />;
export const Size = () => <SentimentVeryDissatisfied height="50" width="50" />;
export const CustomStyle = () => <SentimentVeryDissatisfied style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SentimentVeryDissatisfied className="custom-class" />;

export const Clickable = () => <SentimentVeryDissatisfied onClick={()=>console.log("clicked")} />;

const Template = (args) => <SentimentVeryDissatisfied {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
