import React from "react";
import { SentimentSatisfiedAlt } from "./SentimentSatisfiedAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SentimentSatisfiedAlt",
  component: SentimentSatisfiedAlt,
};

export const Default = () => <SentimentSatisfiedAlt />;
export const Fill = () => <SentimentSatisfiedAlt fill="blue" />;
export const Size = () => <SentimentSatisfiedAlt height="50" width="50" />;
export const CustomStyle = () => <SentimentSatisfiedAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SentimentSatisfiedAlt className="custom-class" />;

export const Clickable = () => <SentimentSatisfiedAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <SentimentSatisfiedAlt {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
