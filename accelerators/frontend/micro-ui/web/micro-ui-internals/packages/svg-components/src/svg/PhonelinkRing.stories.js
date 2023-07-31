import React from "react";
import { PhonelinkRing } from "./PhonelinkRing";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PhonelinkRing",
  component: PhonelinkRing,
};

export const Default = () => <PhonelinkRing />;
export const Fill = () => <PhonelinkRing fill="blue" />;
export const Size = () => <PhonelinkRing height="50" width="50" />;
export const CustomStyle = () => <PhonelinkRing style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PhonelinkRing className="custom-class" />;

export const Clickable = () => <PhonelinkRing onClick={()=>console.log("clicked")} />;

const Template = (args) => <PhonelinkRing {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
