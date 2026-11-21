import React from "react";
import { OnlinePrediction } from "./OnlinePrediction";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "OnlinePrediction",
  component: OnlinePrediction,
};

export const Default = () => <OnlinePrediction />;
export const Fill = () => <OnlinePrediction fill="blue" />;
export const Size = () => <OnlinePrediction height="50" width="50" />;
export const CustomStyle = () => <OnlinePrediction style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <OnlinePrediction className="custom-class" />;

export const Clickable = () => <OnlinePrediction onClick={()=>console.log("clicked")} />;

const Template = (args) => <OnlinePrediction {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
