import React from "react";
import { SentimentVerySatisfied } from "./SentimentVerySatisfied";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SentimentVerySatisfied",
  component: SentimentVerySatisfied,
};

export const Default = () => <SentimentVerySatisfied />;
export const Fill = () => <SentimentVerySatisfied fill="blue" />;
export const Size = () => <SentimentVerySatisfied height="50" width="50" />;
export const CustomStyle = () => <SentimentVerySatisfied style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SentimentVerySatisfied className="custom-class" />;

export const Clickable = () => <SentimentVerySatisfied onClick={()=>console.log("clicked")} />;

const Template = (args) => <SentimentVerySatisfied {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
