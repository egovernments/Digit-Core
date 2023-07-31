import React from "react";
import { RateReview } from "./RateReview";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RateReview",
  component: RateReview,
};

export const Default = () => <RateReview />;
export const Fill = () => <RateReview fill="blue" />;
export const Size = () => <RateReview height="50" width="50" />;
export const CustomStyle = () => <RateReview style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RateReview className="custom-class" />;

export const Clickable = () => <RateReview onClick={()=>console.log("clicked")} />;

const Template = (args) => <RateReview {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
