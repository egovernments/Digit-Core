import React from "react";
import { Deck } from "./Deck";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Deck",
  component: Deck,
};

export const Default = () => <Deck />;
export const Fill = () => <Deck fill="blue" />;
export const Size = () => <Deck height="50" width="50" />;
export const CustomStyle = () => <Deck style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Deck className="custom-class" />;

export const Clickable = () => <Deck onClick={()=>console.log("clicked")} />;

const Template = (args) => <Deck {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
