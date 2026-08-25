import React from "react";
import { SentimentDissatisfied } from "./SentimentDissatisfied";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SentimentDissatisfied",
  component: SentimentDissatisfied,
};

export const Default = () => <SentimentDissatisfied />;
export const Fill = () => <SentimentDissatisfied fill="blue" />;
export const Size = () => <SentimentDissatisfied height="50" width="50" />;
export const CustomStyle = () => <SentimentDissatisfied style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SentimentDissatisfied className="custom-class" />;

export const Clickable = () => <SentimentDissatisfied onClick={()=>console.log("clicked")} />;

const Template = (args) => <SentimentDissatisfied {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
