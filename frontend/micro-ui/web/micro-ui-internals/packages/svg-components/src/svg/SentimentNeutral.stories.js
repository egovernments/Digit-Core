import React from "react";
import { SentimentNeutral } from "./SentimentNeutral";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SentimentNeutral",
  component: SentimentNeutral,
};

export const Default = () => <SentimentNeutral />;
export const Fill = () => <SentimentNeutral fill="blue" />;
export const Size = () => <SentimentNeutral height="50" width="50" />;
export const CustomStyle = () => <SentimentNeutral style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SentimentNeutral className="custom-class" />;

export const Clickable = () => <SentimentNeutral onClick={()=>console.log("clicked")} />;

const Template = (args) => <SentimentNeutral {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
